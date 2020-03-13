package ds.hdfs;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
//import ds.hdfs.INameNode;

public class Client
{
    //Variables Required
    public INameNode nameNode; //Name Node stub
    public IDataNode dataNode; //Data Node stub
    public int blockSize;

    public Client() throws IOException, NotBoundException {
        Map<String, String> config = Utils.parseConfigFile("src/cn_config.txt");
        blockSize = Integer.parseInt(config.get("BLOCK_SIZE"));
        nameNode = Utils.connectNameNode();
        //Get the Name Node Stub
        //nn_details contain NN details in the format Server;IP;Port
    }

    public void PutFile(String Filename) {
        System.out.println("Going to put file" + Filename);

        try {
            // Call NameNode for Open File Request
            System.out.println("Going to open file");
            
            try {
                Operations.OpenCloseResponse response = doOpenClose(Filename, Operations.FileMode.WRITE, true);
                System.out.println(response);

                if(response.getStatus() != Operations.StatusCode.OK) {
                    System.err.println(getErrorMessage(response.getStatus()));
                    return;
                }
            } catch (RemoteException e) {
                System.err.println("Failed to open remote file for writing");
                return;
            }

            List<Operations.DataNode> nodeList;
            int replicationFactor;

            System.out.println("Going to assign blocks");

            try {
                // Call NameNode for Assign Block Request
                Operations.AssignBlockRequest blockRequest = Operations.AssignBlockRequest
                        .newBuilder()
                        .build();

                Operations.AssignBlockResponse blockResponse = Operations.AssignBlockResponse
                        .parseFrom(nameNode.assignBlock(blockRequest.toByteArray()));

                if(blockResponse.getStatus() != Operations.StatusCode.OK) {
                    System.err.println(getErrorMessage(blockResponse.getStatus()));
                    return;
                }

                nodeList = blockResponse.getNodesList();
                replicationFactor = blockResponse.getReplicationFactor();
            } catch (RemoteException e) {
                System.err.println("Failed assign blocks for file");
                return;
            }

            // Get Stubs for each individual DataNode
            List<IDataNode> stubList = new ArrayList<>();

            for (Operations.DataNode dn : nodeList) {
                System.out.println(dn);
                try {
                    stubList.add(connectDataNode(dn.getIp(), dn.getPort(), "IDataNode-" + dn.getId()));
                } catch (NotBoundException | RemoteException e) {
                    System.err.println(
                        String.format("Could not connect to DataNode %d, continuing anyways", dn.getId())
                    );
                }
            }


            // Open file and put into input stream
            try {
                InputStream input = new FileInputStream(Filename);
                DataInputStream dataInputStream = new DataInputStream(input);
                int fileIndex = 0; // file offset
                int blockIndex = 0; // block number
                int nodeIndex = 0; // node index in list

                while (true) {
                    byte[] curr = new byte[blockSize];
                    int numRead = dataInputStream.read(curr, fileIndex, blockSize); // returns bytes read (len)
                    if (numRead <= 0) break;
                    fileIndex += numRead;

                    int replicationCount = 0;
                    while (replicationCount < replicationFactor) {
                        try {
                            Operations.ReadWriteResponse writeResponse = doReadWrite(
                                    Filename,
                                    blockIndex,
                                    ByteString.copyFrom(curr),
                                    Operations.FileMode.WRITE,
                                    stubList.get(nodeIndex)
                            );

                            if(writeResponse.getStatus() != Operations.StatusCode.OK) {
                                System.err.println(getErrorMessage(writeResponse.getStatus()));
                            } else {
                                replicationCount++;
                            }
                        } catch (RemoteException e) {
                            // ignore
                        }
                        nodeIndex++;
                        if(nodeIndex >= nodeList.size()) {
                            if (replicationCount < replicationFactor)
                                System.err.printf("Failed to reach replication factor for block %d\n", blockIndex);
                            break;
                        }
                    }
                    blockIndex++;
                }
                dataInputStream.close();

            } catch (IOException e) {
                System.err.println("Failed to find or read file");
                return;
            }

            try {
                Operations.OpenCloseResponse response = doOpenClose(Filename, Operations.FileMode.WRITE, false);

                if(response.getStatus() != Operations.StatusCode.OK) {
                    System.err.println(getErrorMessage(response.getStatus()));
                    return;
                }
            } catch (RemoteException e) {
                System.err.println("Failed to close file");
                return;
            }

            System.out.println("Successfully wrote file.");

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void GetFile(String filename) {
        try {
            // Call NameNode for Open File Request
            try {
                Operations.OpenCloseResponse response = doOpenClose(filename, Operations.FileMode.READ, true);

                if(response.getStatus() != Operations.StatusCode.OK) {
                    System.err.println(getErrorMessage(response.getStatus()));
                    return;
                }
            } catch (RemoteException e) {
                System.err.println("Failed to open remote file for reading");
                return;
            }

            int blockNumber = 0;
            File outputFile;
            FileOutputStream outputStream;
            Map<Integer, IDataNode> stubs = new HashMap<>();

            try {
                outputFile = new File(filename);
                outputFile.createNewFile();
                outputStream = new FileOutputStream(filename);
            } catch (IOException e) {
                System.err.println("Failed to open local file for writing");
                return;
            }

            while(true) {
                byte[] contents = null;
                Operations.GetBlockLocationsResponse locationsResponse;

                try {
                    byte[] locationsRequest = Operations.GetBlockLocationsRequest
                            .newBuilder()
                            .setFilename(filename)
                            .setBlockNumber(blockNumber)
                            .build()
                            .toByteArray();

                    locationsResponse = Operations.GetBlockLocationsResponse
                            .parseFrom(nameNode.getBlockLocations(locationsRequest));

                    // If we reach the end of the file, we get an E_NOBLK error
                    if (locationsResponse.getStatus() == Operations.StatusCode.E_NOBLK) {
                        break;
                    } else if (locationsResponse.getStatus() != Operations.StatusCode.OK) {
                        System.err.println(getErrorMessage(locationsResponse.getStatus()));
                        outputFile.delete();
                        break;
                    }
                } catch (RemoteException e) {
                    System.err.println("Failed to query NameNode for block locations, aborting operation");
                    outputFile.delete();
                    break;
                }

                for(Operations.DataNode node : locationsResponse.getNodesList()) {
                    try {
                        if(!stubs.containsKey(node.getId())) {
                            stubs.put(node.getId(), connectDataNode(
                                    node.getIp(),
                                    node.getPort(),
                                    "IDataNode-" + node.getId()
                            ));
                        }

                        Operations.ReadWriteResponse readResponse = doReadWrite(
                                filename,
                                blockNumber,
                                null,
                                Operations.FileMode.READ,
                                stubs.get(node.getId())
                        );

                        if(readResponse.getStatus() == Operations.StatusCode.OK) {
                            contents = readResponse.getContents().toByteArray();
                            break;  // only need to successfully read from one node
                        }
                    } catch (RemoteException | NotBoundException e) {
                        // ignore
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

            outputStream.close();

            try {
                Operations.OpenCloseResponse response = doOpenClose(filename, Operations.FileMode.READ, false);
                if(response.getStatus() != Operations.StatusCode.OK) {
                    System.err.println(getErrorMessage(response.getStatus()));
                    return;
                }
            } catch (RemoteException e) {
                System.err.println("Failed to close remote file for reading");
                return;
            }

            System.out.printf("Successfully read file %s\n", filename);
        } catch (InvalidProtocolBufferException e) {
            System.err.println("Protobuf error");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void List() {
        try {
            Operations.ListResponse response = Operations.ListResponse.parseFrom(nameNode.list());
            for (String filename : response.getFilenamesList()) {
                System.out.println(filename);
            }
        } catch (RemoteException e) {
            System.err.println("Failed to access list of remote files");
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private static IDataNode connectDataNode(String ip, int port, String name) throws RemoteException, NotBoundException {
        Registry serverRegistry = LocateRegistry.getRegistry(ip, NameNode.REGISTRY_PORT);
        return (IDataNode) serverRegistry.lookup(name);
    }

    private Operations.OpenCloseResponse doOpenClose(String filename, Operations.FileMode mode, boolean isOpen)
            throws InvalidProtocolBufferException, RemoteException {

        byte[] request = Operations.OpenCloseRequest
                .newBuilder()
                .setFilename(filename)
                .setMode(mode)
                .build()
                .toByteArray();

        byte[] response = (isOpen)
                ? nameNode.openFile(request)
                : nameNode.closeFile(request);

        return Operations.OpenCloseResponse.parseFrom(response);
    }

    private Operations.ReadWriteResponse doReadWrite(
            String filename,
            int blockNumber,
            ByteString contents,
            Operations.FileMode mode,
            IDataNode node
    ) throws InvalidProtocolBufferException, RemoteException {
        Operations.ReadWriteRequest request = Operations.ReadWriteRequest
                .newBuilder()
                .setFilename(filename)
                .setBlockNumber(blockNumber)
                .setContents(contents)
                .build();

        byte[] response = mode == Operations.FileMode.WRITE
                ? node.writeBlock(request.toByteArray())
                : node.readBlock(request.toByteArray());

        return Operations.ReadWriteResponse.parseFrom(response);
    }

    /**
     *
     * E_NOENT = 3;    // File does not exist
     *     E_NOBLK = 4;    // Block does not exist
     *     E_EXIST = 5;    // File already exists
     *     E_IO    = 6;    // I/O Error
     *     E_INVAL = 7;    // Invalid arguments
     *     E_BUSY  = 8;    // File is being written to
     */
    private static String getErrorMessage(Operations.StatusCode code) {
        switch (code) {
            case E_UNKWN: return "Unknown error";
            case E_NOENT: return "File does not exist";
            case E_NOBLK: return "End of file";
            case E_EXIST: return "File already exists";
            case E_IO:    return "Failed to perform I/O operation";
            case E_INVAL: return "Invalid request parameters";
            case E_BUSY:  return "File has already been opened by other clients with a different file mode";
            default:      return "";
        }
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        // To read config file and Connect to NameNode
        //Intitalize the Client
        Client Me = new Client();
        System.out.println("Welcome to HDFS!!");
        Scanner Scan = new Scanner(System.in);
        while(true)
        {
            //Scanner, prompt and then call the functions according to the command
            System.out.print("$> "); //Prompt
            String Command = Scan.nextLine();
            String[] Split_Commands = Command.split(" ");

            if(Split_Commands[0].equals("help"))
            {
                System.out.println("The following are the Supported Commands");
                System.out.println("1. put filename ## To put a file in HDFS");
                System.out.println("2. get filename ## To get a file in HDFS"); System.out.println("2. list ## To get the list of files in HDFS");
            }
            else if(Split_Commands[0].equals("put"))  // put Filename
            {
                //Put file into HDFS
                String Filename;
                try{
                    Filename = Split_Commands[1];
                    Me.PutFile(Filename);
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Please type 'help' for instructions");
                    continue;
                }
            }
            else if(Split_Commands[0].equals("get"))
            {
                //Get file from HDFS
                String Filename;
                try{
                    Filename = Split_Commands[1];
                    Me.GetFile(Filename);
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Please type 'help' for instructions");
                    continue;
                }
            }
            else if(Split_Commands[0].equals("list"))
            {
                //Get list of files in HDFS
                Me.List();
            }
            else
            {
                System.out.println("Please type 'help' for instructions");
            }
        }
    }
}
