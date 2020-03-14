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
    private int id;
    private int registryPort;
    private INameNode nameNode;
    private long failedHeartbeats = 0;
    private Timer timer = new Timer();

    public DataNode(int id, String ip, int registryPort) throws IOException, NotBoundException {
        this.id = id;
        this.ip = ip;
        this.registryPort = registryPort;
        this.nameNode = Utils.connectNameNode();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendHeartbeat();
                } catch (RemoteException re) {
                    failedHeartbeats++;
                    System.err.println("Failed to send heartbeat");

                    // If we fail to send heartbeats multiple times
                    // in a row, try to reconnect to NameNode
                    if(failedHeartbeats % 10 == 0) {
                        try {
                            nameNode = Utils.connectNameNode();
                        } catch (Exception e) {
                            System.err.println("Failed to connect to NameNode");
                        }
                    }
                }
            }
        }, 0, HEARTBEAT_INTERVAL);
    }

    public static void main(String[] args) {
        try {
            // Get NameNode's configuration
            Map<String, String> config = Utils.parseConfigFile(args[0]);
            int id = Integer.parseInt(config.get("ID"));
            String ip = config.get("IP");
            int registryPort = Integer.parseInt(config.get("REGISTRY-PORT"));
            int stubPort = Integer.parseInt(config.get("STUB-PORT"));

            // Bind remote object's stub in registry
            DataNode node = new DataNode(id, ip, registryPort);
            IDataNode stub = (IDataNode) UnicastRemoteObject.exportObject(node, stubPort);
            String nodeName = String.format("IDataNode-%d", id);
            Registry localRegistry = bindStub(nodeName, stub);

            // We need to unbind the DataNode from the registry
            // in case it needs to restart (in which case it will have
            // the same name and the registry will refuse to bind it)
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                try {
//                    localRegistry.unbind(nodeName);
//                } catch (Exception e) {
//                    // error will not occur
//                    e.printStackTrace();
//                }
//            }));

            System.out.println(String.format("DataNode ready! (IP %s, PORT %d)", ip, stubPort));
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.err.println("DataNode is already bound to the specified registryPort");
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.err.println("NameNode is not up");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read data node config file");
        }
    }


    private static Registry bindStub(String nodeName, IDataNode stub)
            throws AlreadyBoundException, RemoteException {
        try {
            Registry localRegistry = LocateRegistry.getRegistry(NameNode.REGISTRY_PORT);
            localRegistry.bind(nodeName, stub);
            return localRegistry;
        } catch (ConnectException e) {
            Registry localRegistry = LocateRegistry.createRegistry(NameNode.REGISTRY_PORT);
            localRegistry.bind(nodeName, stub);
            return localRegistry;
        }
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

        String blockFileName = String.format("data/node-%d/%s.%s", 
                id, fileName, blockNumber);
        File file = new File(blockFileName);

        if(!file.exists()) {
            return createReadWriteResponse(Operations.StatusCode.E_INVAL);
        }

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

        String blockFileName = String.format("data/node-%d/%s.%s", 
                id, fileName, blockNumber);
        File file = new File(blockFileName);

        if(!file.exists()) {
            try {
                Path pathToFile = Paths.get(blockFileName);
                Files.createDirectories(pathToFile.getParent());
                Files.createFile(pathToFile);
            } catch (IOException e) {
                System.err.println("Failed to create block file");
                e.printStackTrace();
                return createReadWriteResponse(Operations.StatusCode.E_IO);
            }
        }

        try (OutputStream outputStream = new FileOutputStream(new File(blockFileName))) {
            outputStream.write(contents);
        } catch (Exception e) {
            e.printStackTrace();
            return createReadWriteResponse(Operations.StatusCode.E_IO);
        }

        return createReadWriteResponse(Operations.StatusCode.OK);
    }

    public void sendHeartbeat() throws RemoteException {
        File folder = new File(String.format("data/node-%d/", id));
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
                            .setPort(registryPort)
                            .build())
                .addAllAvailableFileBlocks(blocks)
                .build()
                .toByteArray();

        nameNode.heartBeat(heartbeat);
    }

    private static byte[] createReadWriteResponse(Operations.StatusCode status) {
        return Operations.ReadWriteResponse
                .newBuilder()
                .setStatus(status)
                .build()
                .toByteArray();
    }
}
