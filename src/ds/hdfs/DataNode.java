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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import ds.hdfs.IDataNode.*;

public class DataNode implements IDataNode {
    protected String MyChunksFile;
    protected INameNode NNStub;
    protected String ip;
    protected int port;
    protected String name;
    protected int id;

    public DataNode() {
        //Constructor
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

    @Override
    public byte[] blockReport(byte[] inp) throws RemoteException {
        File folder = new File("data/node1");

        List<Operations.FileBlock> blocks = Arrays.stream(folder.listFiles())
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

        return Operations.Heartbeat
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

    public static void main(String args[]) throws InvalidProtocolBufferException, IOException {
        //Define a Datanode Me
        DataNode Me = new DataNode();
        byte[] arr = new byte[10];
        Me.blockReport(arr);
    }
}
