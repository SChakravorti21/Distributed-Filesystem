//Written By Shaleen Garg
package ds.hdfs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.util.*;

import com.google.protobuf.ByteString;

import java.io.*;
import java.util.stream.Collectors;

public class DataNode implements IDataNode {
    private static final long HEARTBEAT_INTERVAL = 250;

    private String ip;
    private int port;
    private int id;
    private INameNode nameNode;
    private Timer timer = new Timer();

    public DataNode(int id, String ip, int port, INameNode nameNode) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nameNode = nameNode;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendHeartbeat();
                } catch (RemoteException e) {
                    System.err.println("Failed to send heartbeat");
                }
            }
        }, 0, HEARTBEAT_INTERVAL);
    }

    public static void main(String[] args) {
        try {
            // Get NameNode's configuration
            Map<String, String> config = parseConfigFile(args[0]);
            int id = Integer.parseInt(config.get("ID"));
            String ip = config.get("IP");
            int port = Integer.parseInt(config.get("PORT"));

            // DataNode needs access to NameNode for sending heartbeats
            String registryHost = parseConfigFile("src/nn_config.txt").get("IP");
            Registry serverRegistry = LocateRegistry.getRegistry(registryHost, NameNode.REGISTRY_PORT);
            INameNode nameNode = (INameNode) serverRegistry.lookup("INameNode");

            // Bind remote object's stub in registry
            DataNode node = new DataNode(id, ip, port, nameNode);
            IDataNode stub = (IDataNode) UnicastRemoteObject.exportObject(node, port);
            String nodeName = String.format("IDataNode-%d", id);
            serverRegistry.bind(nodeName, stub);

            // We need to unbind the DataNode from the registry
            // in case it needs to restart (in which case it will have
            // the same name and the registry will refuse to bind it)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverRegistry.unbind(nodeName);
                } catch (Exception e) {
                    // error will not occur
                }
            }));

            System.out.println(String.format("DataNode ready! (IP %s, PORT %d)", ip, port));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read data node config file");
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.err.println("DataNode is already bound to the specified port");
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.err.println("NameNode is not up");
        }
    }

    private static Map<String, String> parseConfigFile(String filename) throws IOException {
        return Files.lines(Paths.get(filename))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(
                        line -> line[0],
                        line -> line[1]
                ));
    }

    public static void appendtoFile(String Filename, String Line) {
        BufferedWriter bw = null;
//
//        try {
//            //append
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        } finally {                       // always close the file
//            if (bw != null) try {
//                bw.close();
//            } catch (IOException ioe2) {
//            }
//        }

    }

    public byte[] readBlock(byte[] Inp) {
        // Filename, Block No.
        Operations.ReadWriteRequest request;
        Operations.StatusCode status;

        try {
            request = Operations.ReadWriteRequest.parseFrom(Inp);
        } catch (Exception e) {
            e.printStackTrace();
            return createReadWriteResponse(Operations.StatusCode.E_UNKWN);
        }

        String fileName = request.getFilename();
        long blockNumber = request.getBlockNumber();

        String blockFileName = fileName+"."+blockNumber;
        byte[] data;

        try {
            Path path = Paths.get(blockFileName);
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return createReadWriteResponse(Operations.StatusCode.E_IO);
        }

        ByteString dataStr = ByteString.copyFrom(data);

        Operations.ReadWriteResponse response = Operations.ReadWriteResponse
                .newBuilder()
                .setContents(dataStr)
                .setStatus(Operations.StatusCode.OK)
                .build();

        return response.toByteArray();
    }

    public byte[] writeBlock(byte[] Inp) {
        // Filename, Block No. and Block Contents
        Operations.ReadWriteRequest request;
        Operations.StatusCode status;

        try {
            request = Operations.ReadWriteRequest.parseFrom(Inp);
        } catch (Exception e) {
            e.printStackTrace();
            return createReadWriteResponse(Operations.StatusCode.E_UNKWN);
        }

        String fileName = request.getFilename();
        long blockNumber = request.getBlockNumber();
        byte[] contents = request.getContents().toByteArray();

        String blockFileName = fileName+"."+blockNumber;

        try (OutputStream outputStream = new FileOutputStream(new File(blockFileName))) {
            outputStream.write(contents);
        } catch (Exception e) {
            e.printStackTrace();
            return createReadWriteResponse(Operations.StatusCode.E_IO);
        }

        return createReadWriteResponse(Operations.StatusCode.OK);
    }

    public void sendHeartbeat() throws RemoteException {
        File folder = new File("data/node1");
        File[] files = folder.listFiles();

        if(files == null)
            files = new File[0];

        List<Operations.FileBlock> blocks = Arrays.stream(files)
                .map(File::getName)
                .map(blockFilename -> {
                    int extensionIndex = blockFilename.lastIndexOf(".");
                    String filename = blockFilename.substring(0, extensionIndex);
                    String blockNumber = blockFilename.substring(extensionIndex + 1);

                    return Operations.FileBlock.newBuilder()
                            .setFilename(filename)
                            .setFileBlock(Integer.parseInt(blockNumber))
                            .build();
                }).collect(Collectors.toList());

        byte[] heartbeat = Operations.Heartbeat
                .newBuilder()
                .setNode(Operations.DataNode
                            .newBuilder()
                            .setId(id)
                            .setIp(ip)
                            .setPort(port)
                            .build())
                .addAllAvailableFileBlocks(blocks)
                .build()
                .toByteArray();

        nameNode.heartBeat(heartbeat);
    }

    public void BlockReport() throws IOException {
    }

    public void BindServer(String Name, String IP, int Port) {
        try {
            IDataNode stub = (IDataNode) UnicastRemoteObject.exportObject(this, 0);
            System.setProperty("java.rmi.server.hostname", IP);
            Registry registry = LocateRegistry.getRegistry(Port);
            registry.rebind(Name, stub);
            System.out.println("\nDataNode connected to RMIregistry\n");
        } catch (Exception e) {
            System.err.println("Server Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public INameNode GetNNStub(String Name, String IP, int Port) {
        while (true) {
            try {
                Registry registry = LocateRegistry.getRegistry(IP, Port);
                INameNode stub = (INameNode) registry.lookup(Name);
                System.out.println("NameNode Found!");
                return stub;
            } catch (Exception e) {
                System.out.println("NameNode still not Found");
                continue;
            }
        }
    }

    private static byte[] createReadWriteResponse(Operations.StatusCode status) {
        return Operations.ReadWriteResponse
                .newBuilder()
                .setStatus(status)
                .build()
                .toByteArray();
    }
}
