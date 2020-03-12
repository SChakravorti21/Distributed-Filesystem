package ds.hdfs;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
//import ds.hdfs.INameNode;

public class Client
{
    //Variables Required
    public INameNode nameNode; //Name Node stub
    public IDataNode dataNode; //Data Node stub
    public int blockSize;
    public Client()
    {
        try {
            Map<String, String> config = Utils.parseConfigFile("src/cn_config.txt");
            blockSize = Integer.parseInt(config.get("BLOCK_SIZE"));
            nameNode = connectNameNode();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        //Get the Name Node Stub
        //nn_details contain NN details in the format Server;IP;Port
    }

    public void PutFile(String Filename) //Put File
    {
        System.out.println("Going to put file" + Filename);
        ArrayList<byte[]> blocks = new ArrayList<byte[]>();

        try {
            // Read file in from local disk
            Path path = Paths.get(Filename);
            byte[] data = Files.readAllBytes(path);

            // Divide file into multiple blocks
            int fileInd = 0;
            while (true) {
                byte[] currBlock = new byte[blockSize];
                int byteCount = 0;

                while (byteCount < blockSize && fileInd < data.length) {
                    currBlock[byteCount] = data[fileInd];
                    byteCount++;
                    fileInd++;
                }
                blocks.add(currBlock);

                if (fileInd >= data.length) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Call NameNode for Open File Request
            Operations.OpenCloseRequest request = Operations.OpenCloseRequest
                    .newBuilder()
                    .setFilename(Filename)
                    .setMode(Operations.FileMode.WRITE)
                    .build();

            Operations.OpenCloseResponse response = Operations.OpenCloseResponse
                    .parseFrom(nameNode.openFile(request.toByteArray()));
            Operations.StatusCode status = response.getStatus();

            if (status == Operations.StatusCode.OK) {

                // Call NameNode for Assign Block Request
                Operations.AssignBlockRequest blockRequest = Operations.AssignBlockRequest
                        .newBuilder()
                        .setFilename(Filename)
                        .setBlockNumber(blocks.size())
                        .build();

                Operations.AssignBlockResponse blockResponse = Operations.AssignBlockResponse
                        .parseFrom(nameNode.assignBlock(blockRequest.toByteArray()));

                List<Operations.DataNode> nodeList = blockResponse.getNodesList();
                int repFactor = blockResponse.getReplicationFactor();

                // Get Stubs for each individual DataNode
                List<IDataNode> stubList = new ArrayList<IDataNode>();

                for (Operations.DataNode dn : nodeList) {
                    try {
                        stubList.add(connectDataNode(dn.getIp(), dn.getPort(), "IDataNode"));
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }

                // Begin writing blocks to each DataNode
                boolean finishedWrite = false;
                int[] replication = new int[blocks.size()];
                Arrays.fill(replication, repFactor);
                int nodeIndex = 0;
                int blockIndex = 0;

                while (blockIndex < blocks.size()) {
                    Operations.ReadWriteRequest writeRequest = Operations.ReadWriteRequest
                            .newBuilder()
                            .setFilename(Filename)
                            .setBlockNumber(blockIndex)
                            .setContents(ByteString.copyFrom(blocks.get(blockIndex)))
                            .build();

                    Operations.ReadWriteResponse writeResponse = Operations.ReadWriteResponse
                            .parseFrom(stubList.get(nodeIndex).writeBlock(writeRequest.toByteArray()));

                    if(writeResponse.getStatus() != Operations.StatusCode.OK) {
                        // Error Checking for Failed Write
                    }

                    nodeIndex++;
                    nodeIndex %= nodeList.size();
                    replication[blockIndex]--;
                    if(replication[blockIndex] == 0) blockIndex++;
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }


//        try{
//            bis = new BufferedInputStream(new FileInputStream(File));
//        }catch(Exception e){
//            System.out.println("File not found !!!");
//            return;
//        }
        /*
            [Node4, Node2, Node3, Node1], replication factor = 2
            [success, failure, null, null]
         */

//        List<String> list;
//        for(int i = 0; i < list.size() * 3; i++) {
//            int nodeIndex = i % list.size();
//        }
    }

    public void GetFile(String FileName)
    {
        try {
            // Call NameNode for Open File Request
            Operations.OpenCloseRequest request = Operations.OpenCloseRequest
                    .newBuilder()
                    .setFilename(FileName)
                    .setMode(Operations.FileMode.READ)
                    .build();

            Operations.OpenCloseResponse response = Operations.OpenCloseResponse
                    .parseFrom(nameNode.openFile(request.toByteArray()));

            if(response.getStatus() != Operations.StatusCode.OK) {
                //Error Check
            }




        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void List()
    {

    }

    private static INameNode connectNameNode() throws IOException, NotBoundException {
        String registryHost = Utils.parseConfigFile("src/nn_config.txt").get("IP");
        Registry serverRegistry = LocateRegistry.getRegistry(registryHost, NameNode.REGISTRY_PORT);
        return (INameNode) serverRegistry.lookup("INameNode");
    }

    private static IDataNode connectDataNode(String ip, int port, String name) throws RemoteException, NotBoundException {
        Registry serverRegistry = LocateRegistry.getRegistry(ip, port);
        return (IDataNode) serverRegistry.lookup(name);
    }

    public static void main(String[] args) throws RemoteException, UnknownHostException
    {
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
                System.out.println("List request");
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
