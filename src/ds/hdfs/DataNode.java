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

import ds.hdfs.IDataNode.*;

public class DataNode implements IDataNode {
    protected String MyChunksFile;
    protected INameNode NNStub;
    protected String MyIP;
    protected int MyPort;
    protected String MyName;
    protected int MyID;

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
        File[] listOfFiles = folder.listFiles();
        HashMap<String, Operations.FileBlocks.Builder> map = new HashMap<String, Operations.FileBlocks.Builder>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fullFileName = listOfFiles[i].getName();
                int lastIndex = fullFileName.lastIndexOf(".");

                String fileName = fullFileName.substring(0, lastIndex);
                long index = Long.valueOf(fullFileName.substring(lastIndex+1));

                Operations.FileBlocks.Builder def = Operations.FileBlocks.newBuilder().setFilename(fileName);
                Operations.FileBlocks.Builder curr = map.getOrDefault(fileName, def);
                curr.addFileBlocks(index);

//                System.out.println("File: " + filename.substring(0, lastIndex));
//                System.out.println("Block: " + filename.substring(lastIndex+1));
            }
        }

        Operations.Heartbeat.Builder heartbeat = Operations.Heartbeat.newBuilder();
        for(String key : map.keySet()) {
            heartbeat.addAvailableFileBlocks( (Operations.FileBlocks.Builder) map.get(key) );
        }

        // Add IP, Port
        Operations.DataNode.Builder node = Operations.DataNode.newBuilder();
        heartbeat.setNode(node);

        return heartbeat.build().toByteArray();
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
