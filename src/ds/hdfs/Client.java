package ds.hdfs;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import ds.hdfs.proto.IDataNodeGrpc;
import ds.hdfs.proto.INameNodeGrpc;
import ds.hdfs.proto.Operations;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Client {

    private int blockSize;
    private INameNodeGrpc.INameNodeBlockingStub nameNode;

    private Client() throws IOException {
        Map<String, String> config = Utils.parseConfigFile("src/cn_config.txt");
        blockSize = Integer.parseInt(config.get("BLOCK_SIZE"));
        nameNode = Utils.getNameNodeStub();
    }

    /**
     * @param localFilename the file on client's filesystem to upload to HDFS
     * @param remoteFilename the name with which the uploaded file should be saved
     * @throws StatusRuntimeException if contacting the NameNode fails
     * @throws IOException if performing I/O on {@param localFilename} fails
     */
    private void putFile(String localFilename, String remoteFilename) throws StatusRuntimeException, IOException {
        System.out.printf("Going to write local file (%s) to remote file (%s)\n",
                localFilename, remoteFilename);

        // Open the local file that needs to be read
        InputStream input;
        DataInputStream dataInputStream;

        try {
            input = new FileInputStream(localFilename);
            dataInputStream = new DataInputStream(input);
        } catch (IOException e) {
            System.err.println("Failed to open local file for reading");
            return;
        }

        // Ask permission to open the remote file for writing
        Operations.OpenCloseResponse openResponse = doOpenClose(remoteFilename, Operations.FileMode.WRITE, true);

        if(openResponse.getStatus() != Operations.StatusCode.OK) {
            System.err.println(getErrorMessage(openResponse.getStatus()));
            dataInputStream.close();
            return;
        }

        // Ask the NameNode for available DataNodes we can write blocks to.
        // NameNode shuffles the list of DataNodes to ensure some degree of load balancing
        Operations.AssignBlockRequest blockRequest = Operations.AssignBlockRequest.getDefaultInstance();
        Operations.AssignBlockResponse blockResponse = nameNode.assignBlock(blockRequest);

        if(blockResponse.getStatus() != Operations.StatusCode.OK) {
            System.err.println(getErrorMessage(blockResponse.getStatus()));
            dataInputStream.close();
            return;
        }

        int blockIndex = 0; // block number
        int replicationFactor = blockResponse.getReplicationFactor();

        // We can create stubs for all the DataNodes right away because
        // gRPC doesn't connect to the service until a procedure call is made
        List<IDataNodeGrpc.IDataNodeBlockingStub> stubs = blockResponse.getNodesList()
                .stream()
                .map(node -> Utils.getDataNodeStub(node.getIp(), node.getPort()))
                .collect(Collectors.toList());

        // We break out of the loop when we reach the end of the file.
        while (true) {
            // Read the current block from the file
            int replicationCount = 0;
            byte[] curr = new byte[blockSize];
            int numRead = dataInputStream.read(curr); // returns bytes read (len)

            // Bytes read is -1 if we reach EOF
            if (numRead <= 0) break;

            // Replicate the block on $replicationFactor nodes, or as many nodes
            // as possible if replication factor cannot be reached
            for(int nodeIndex = 0;
                nodeIndex < stubs.size() && replicationCount < replicationFactor;
                nodeIndex++
            ) {
                try {
                    Operations.ReadWriteResponse writeResponse = doReadWrite(
                            remoteFilename,
                            blockIndex,
                            ByteString.copyFrom(curr, 0, numRead),
                            Operations.FileMode.WRITE,
                            stubs.get(nodeIndex)
                    );

                    if(writeResponse.getStatus() != Operations.StatusCode.OK) {
                        System.err.println(getErrorMessage(writeResponse.getStatus()));
                    } else {
                        replicationCount++;
                    }
                } catch (StatusRuntimeException e) {
                    System.err.printf("Could not reach DataNode %d, continuing anyways (error = %s)\n",
                            blockResponse.getNodesList().get(nodeIndex).getId(),
                            e.getStatus().getCode());
                }
            }

            // The project states to assume at least half of all DataNodes will be up
            // at all times, so we do not handle the case where replicationCount = 0.
            if (replicationCount < replicationFactor)
                System.err.printf("Failed to reach replication factor for block %d\n", blockIndex);

            blockIndex++;
        }

        // Close both the local and remote files
        // Closing remote file is especially important because writing
        // acquires an exclusive lock on the file
        dataInputStream.close();
        shutdownStubChannels(stubs);
        Operations.OpenCloseResponse closeResponse = doOpenClose(remoteFilename, Operations.FileMode.WRITE, false);

        if(closeResponse.getStatus() != Operations.StatusCode.OK) {
            System.err.println(getErrorMessage(closeResponse.getStatus()));
        }
    }

    /**
     * @param remoteFilename the HDFS file to download
     * @param localFilename the name with which the downloaded file is stored on local filesystem
     * @throws StatusRuntimeException if contacting the NameNode fails
     * @throws IOException if performing I/O on {@param localFilename} fails
     */
    private void getFile(String remoteFilename, String localFilename) throws StatusRuntimeException, IOException {
        System.out.printf("Going to read remote file (%s) to local file (%s)\n",
                remoteFilename, localFilename);

        // Open the local file to which we will write the contents of the remote file
        File outputFile;
        FileOutputStream outputStream;

        try {
            outputFile = new File(localFilename);
            outputFile.createNewFile();
            outputStream = new FileOutputStream(localFilename);
        } catch (IOException e) {
            System.err.println("Failed to open local file for writing");
            return;
        }

        // Open remote file for reading; NameNode implementation allows
        // for multiple concurrent readers (reader-writer mutual exclusion)
        Operations.OpenCloseResponse openResponse = doOpenClose(remoteFilename, Operations.FileMode.READ, true);

        if(openResponse.getStatus() != Operations.StatusCode.OK) {
            System.err.println(getErrorMessage(openResponse.getStatus()));
            outputStream.close();
            return;
        }

        int blockNumber = 0;
        Map<Integer, IDataNodeGrpc.IDataNodeBlockingStub> stubs = new HashMap<>();

        while(true) {
            byte[] contents = null;

            // Figure out which DataNodes store the current block
            Operations.GetBlockLocationsRequest locationsRequest = Operations.GetBlockLocationsRequest
                    .newBuilder()
                    .setFilename(remoteFilename)
                    .setBlockNumber(blockNumber)
                    .build();

            Operations.GetBlockLocationsResponse locationsResponse = nameNode.getBlockLocations(locationsRequest);

            // If we reach the end of the file, we get an E_NOBLK error, can break safely
            if (locationsResponse.getStatus() == Operations.StatusCode.E_NOBLK) {
                break;
            } else if (locationsResponse.getStatus() != Operations.StatusCode.OK) {
                System.err.println(getErrorMessage(locationsResponse.getStatus()));
                outputFile.delete();
                break;
            }

            // Try to read the block from any of the listed nodes
            // (only one read needs to be successful as all nodes simply store replicas)
            for(Operations.DataNode node : locationsResponse.getNodesList()) {
                if(!stubs.containsKey(node.getId())) {
                    stubs.put(node.getId(), Utils.getDataNodeStub(node.getIp(), node.getPort()));
                }
                
                try {
                    Operations.ReadWriteResponse readResponse = doReadWrite(
                            remoteFilename,
                            blockNumber,
                            null,
                            Operations.FileMode.READ,
                            stubs.get(node.getId())
                    );

                    if(readResponse.getStatus() == Operations.StatusCode.OK) {
                        contents = readResponse.getContents().toByteArray();
                        break;  // only need to successfully read from one node
                    }
                } catch (StatusRuntimeException e) {
                    // e.printStackTrace();
                }
            }

            if(contents == null) {
                System.err.printf("Failed to read block %d from any node, aborting operation\n", blockNumber);
                outputFile.delete();
                break;
            }

            outputStream.write(contents);
            blockNumber++;
        }

        // Close both local and remote files
        // Closing remote file is especially important because failing to close it might
        // prevent other clients from being able to write to it (reader-writer locking)
        outputStream.close();
        shutdownStubChannels(stubs.values());
        Operations.OpenCloseResponse closeResponse = doOpenClose(remoteFilename, Operations.FileMode.READ, false);

        if(closeResponse.getStatus() != Operations.StatusCode.OK) {
            System.err.println(getErrorMessage(closeResponse.getStatus()));
        }
    }

    public void list() throws StatusRuntimeException {
        Operations.ListResponse response = nameNode.list(Empty.getDefaultInstance());

        for (String filename : response.getFilenamesList()) {
            System.out.println(filename);
        }
    }

    private void shutdownStubChannels(Collection<IDataNodeGrpc.IDataNodeBlockingStub> stubs) {
        stubs.forEach(stub -> {
            ManagedChannel channel = (ManagedChannel) stub.getChannel();
            channel.shutdown();
        });
    }

    private Operations.OpenCloseResponse doOpenClose(
            String filename,
            Operations.FileMode mode,
            boolean isOpen
    ) throws StatusRuntimeException {
        Operations.OpenCloseRequest request = Operations.OpenCloseRequest
                .newBuilder()
                .setFilename(filename)
                .setMode(mode)
                .build();

        return (isOpen)
                ? nameNode.openFile(request)
                : nameNode.closeFile(request);
    }

    private Operations.ReadWriteResponse doReadWrite(
            String filename,
            int blockNumber,
            ByteString contents,
            Operations.FileMode mode,
            IDataNodeGrpc.IDataNodeBlockingStub node
    ) throws StatusRuntimeException {
        Operations.ReadWriteRequest.Builder requestBuilder = Operations.ReadWriteRequest
                .newBuilder()
                .setFilename(filename)
                .setBlockNumber(blockNumber);

        if(contents != null)
            requestBuilder.setContents(contents);

        return (mode == Operations.FileMode.WRITE)
                ? node.writeBlock(requestBuilder.build())
                : node.readBlock(requestBuilder.build());
    }

    /**
     *     E_NOENT = 3;    // File does not exist
     *     E_NOBLK = 4;    // Block does not exist
     *     E_EXIST = 5;    // File already exists
     *     E_IO    = 6;    // I/O Error
     *     E_INVAL = 7;    // Invalid arguments
     *     E_BUSY  = 8;    // File is being written to
     */
    private static String getErrorMessage(Operations.StatusCode code) {
        switch (code) {
            case E_UNKWN: return "ERROR: Unknown error";
            case E_NOENT: return "ERROR: File does not exist";
            case E_NOBLK: return "ERROR: End of file";
            case E_EXIST: return "ERROR: File already exists";
            case E_IO:    return "ERROR: Failed to perform I/O operation on server";
            case E_INVAL: return "ERROR: Invalid request parameters";
            case E_BUSY:  return "ERROR: File has already been opened by other clients with a different file mode";
            default:      return "";
        }
    }

    public static void main(String[] args) {
        Client client;

        try {
            client = new Client();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Welcome to HDFS!");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            //Scanner, prompt and then call the functions according to the command
            System.out.print("$> "); //Prompt
            String command = scanner.nextLine();
            String[] splitCommands = command.split(" ");

            if (splitCommands[0].equals("help")) {
                System.out.println("The following are the supported commands (optional arguments in <>)");
                System.out.println("1. put local_filename <hdfs_filename>  # To put a file in HDFS");
                System.out.println("2. get hdfs_filename  <local_filename> # To get a file from HDFS");
                System.out.println("3. list                                # To get the list of files in HDFS");
                System.out.println("4. quit                                # To exit the client shell");
            } else if (splitCommands[0].equals("put")) {
                try {
                    String localFilename = splitCommands[1];
                    String remoteFilename = splitCommands.length > 2 ? splitCommands[2] : localFilename;

                    if (localFilename.endsWith("/") || remoteFilename.contains("/")) {
                        System.err.println("Directory operations are not supported ('/' in filename)");
                        continue;
                    }

                    client.putFile(localFilename, remoteFilename);
                } catch (StatusRuntimeException e) {
                    System.err.printf("Failed to contact NameNode, error = %s \n", e.getStatus().getCode());
                } catch (IOException e) {
                    System.err.println("Local I/O exception occurred, please provide valid paths/filenames");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Please type 'help' for instructions");
                }
            } else if (splitCommands[0].equals("get")) {
                try {
                    String remoteFilename = splitCommands[1];
                    String localFilename = splitCommands.length > 2 ? splitCommands[2] : remoteFilename;
                    client.getFile(remoteFilename, localFilename);
                } catch (StatusRuntimeException e) {
                    System.err.printf("Failed to contact NameNode, error = %s \n", e.getStatus().getCode());
                } catch (IOException e) {
                    System.err.println("Local I/O exception occurred, please provide valid paths/filenames");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Please type 'help' for instructions");
                }
            } else if (splitCommands[0].equals("list")) {
                try {
                    client.list();
                } catch (StatusRuntimeException e) {
                    System.err.printf("Failed to contact NameNode, error = %s \n", e.getStatus().getCode());
                }
            } else if (splitCommands[0].equals("quit") || splitCommands[0].equals("exit")) {
                break;
            } else {
                System.out.println("Please type 'help' for instructions");
            }
        }
    }
}
