package ds.hdfs;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MapEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NameNode implements INameNode {

    // H: Times are always in milliseconds
    // Scan for dead DataNodes every second
    private static final long SCAN_INTERVAL = 1000;

    // How old a heartbeat has to be for the DataNode
    // to be considered dead
    private static final long HEARTBEAT_THRESHOLD = 1000;

    private static String STATE_CACHE_FILE = "data/nn_state.txt";

    // We use the registry to contact DataNodes?
    public static final int REGISTRY_PORT = 1099;


    // NameNode's properties and locks
    private String ipAddress;
    private int portNumber;
    private String identifier;
    private int replicationFactor;    // How many DataNodes a chunk should be replicated on

    private static final Object nodeLock = new Object();
    private static final Object fileLock = new Object();

    // Tracking active DataNodes and their properties
    private Timer timer = new Timer();
    private Map<Integer, DataNode> activeNodes = new HashMap<>();

    // Tracking file information, such what files are available,
    // whether they're being written to, and which nodes store chunks
    private Map<String, FileStatus> fileStatuses;
    private Set<DataNodeBlockInfo> blockInfoList = new HashSet<>();

    public NameNode(String ipAddress, int portNumber, String name, int replicationFactor) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.identifier = name;
        this.replicationFactor = replicationFactor;
        this.fileStatuses = loadState(STATE_CACHE_FILE);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                removeDeadNodes();
                persistState(STATE_CACHE_FILE);
            }
        }, SCAN_INTERVAL, SCAN_INTERVAL);
    }

    public static void main(String[] args) {
        try {
            // Get NameNode's configuration
            Map<String, String> config = Utils.parseConfigFile("src/nn_config.txt");

            String description = config.get("DESCRIPTION");
            String ip = config.get("IP");
            int port = Integer.parseInt(config.get("PORT"));
            int replicationFactor = Integer.parseInt(config.get("REPLICATION_FACTOR"));

            NameNode nameNode = new NameNode(ip, port, description, replicationFactor);
            INameNode skeleton = (INameNode) UnicastRemoteObject.exportObject(nameNode, port);

            // Bind remote object's stub in registry
            Registry serverRegistry = LocateRegistry.createRegistry(REGISTRY_PORT);
            serverRegistry.bind("INameNode", skeleton);
            System.out.println(String.format("NameNode ready! (IP %s, PORT %d)", ip, port));
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.err.println("NameNode already bound to port, check that NameNode is not already running.");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Error instantiating server skeleton");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read config file, check that src/nn_config.txt exists");
        }
    }

    public byte[] openFile(byte[] req)  {
        Operations.OpenCloseRequest request;
        Operations.StatusCode status = Operations.StatusCode.OK;

        try {
            request = Operations.OpenCloseRequest.parseFrom(req);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return createOpenCloseResponse(Operations.StatusCode.E_UNKWN);
        }

        synchronized (fileLock) {
            String filename = request.getFilename();
            Operations.FileMode requestedMode = request.getMode();
            FileStatus fileStatus = fileStatuses.getOrDefault(filename, null);

            if (fileStatus == null) {
                if(requestedMode == Operations.FileMode.READ) {
                    // If the file doesn't exist, it can't be opened
                    status = Operations.StatusCode.E_NOENT;
                } else {
                    // We're writing to the file for the first time
                    fileStatuses.put(filename, new FileStatus(requestedMode, 1));
                }
            } else if (fileStatus.openMode == Operations.FileMode.WRITE) {
                // If ANYONE is writing to the file, no one can read
                // or write to it
                status = Operations.StatusCode.E_BUSY;
            } else if (requestedMode == Operations.FileMode.WRITE
                    && fileStatus.openMode == Operations.FileMode.READ) {
                // If people are reading the file but someone
                // requests to write to it, that would cause reading
                // corrupted/stale data
                status = Operations.StatusCode.E_BUSY;
            } else {
                fileStatus.openMode = requestedMode;
                fileStatus.openHandles++;
            }
        }

        return createOpenCloseResponse(status);
    }

    public byte[] closeFile(byte[] req)  {
        Operations.OpenCloseRequest request;
        Operations.StatusCode status = Operations.StatusCode.OK;

        try {
            request = Operations.OpenCloseRequest.parseFrom(req);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return createOpenCloseResponse(Operations.StatusCode.E_UNKWN);
        }

        synchronized (fileLock) {
            String filename = request.getFilename();
            Operations.FileMode requestedMode = request.getMode();
            FileStatus fileStatus = fileStatuses.getOrDefault(filename, null);

            if (fileStatus == null || fileStatus.openMode != requestedMode) {
                // Invalid argument to close non-existent file
                // or close with incorrect mode
                status = Operations.StatusCode.E_INVAL;
            } else {
                // Decrement number of nodes accessing file.
                // If it reaches 0, clear mode so that it doesn't
                // seem like the file is being read/written to.
                if (--fileStatus.openHandles == 0) {
                    fileStatus.openMode = null;
                }
            }
        }

        return createOpenCloseResponse(status);
    }

    public byte[] getBlockLocations(byte[] req) {
        Operations.GetBlockLocationsRequest request;

        try {
            request = Operations.GetBlockLocationsRequest.parseFrom(req);
        } catch (InvalidProtocolBufferException ex) {
            ex.printStackTrace();
            return createGetBlockLocationsResponse(Operations.StatusCode.E_UNKWN);
        }

        synchronized (nodeLock) {
            synchronized (fileLock) {
                // Check if this is a valid block number for the file
                long maxBlockNumber = fileStatuses.get(request.getFilename()).maxBlockNumber;

                if(request.getBlockNumber() > maxBlockNumber) {
                    return createGetBlockLocationsResponse(Operations.StatusCode.E_NOBLK);
                }

                // For the client's convenience, also filter
                // by active nodes so that client has higher likelihood
                // of contacting a live DataNode.
                List<Operations.DataNode> availableNodes = blockInfoList
                        .stream()
                        .filter(blockInfo -> blockInfo.filename.equals(request.getFilename())
                                && blockInfo.blockNumber == request.getBlockNumber())
                        .map(blockInfo -> blockInfo.node)
                        .filter(node -> activeNodes.containsKey(node.id))
                        .distinct()
                        .map(NameNode::convertDataNodeToProto)
                        .collect(Collectors.toList());

                return createGetBlockLocationsResponse(
                        Operations.StatusCode.OK,
                        availableNodes);
            }
        }
    }


    public byte[] assignBlock(byte[] inp) {
        synchronized (nodeLock) {
            List<DataNode> nodes = new ArrayList<>(activeNodes.values());
            Collections.shuffle(nodes);

            List<Operations.DataNode> assignedNodes = nodes
                    .stream()
                    .map(NameNode::convertDataNodeToProto)
                    .collect(Collectors.toList());

            return Operations.AssignBlockResponse
                    .newBuilder()
                    .setStatus(Operations.StatusCode.OK)
                    .setReplicationFactor(replicationFactor)
                    .addAllNodes(assignedNodes)
                    .build()
                    .toByteArray();
        }
    }


    public byte[] list() {
        synchronized (fileLock) {
            return Operations.ListResponse
                    .newBuilder()
                    .setStatus(Operations.StatusCode.OK)
                    .addAllFilenames(fileStatuses.keySet())
                    .build()
                    .toByteArray();
        }
    }

    public void heartBeat(byte[] req) {
        Operations.Heartbeat heartbeat;

        try {
            heartbeat = Operations.Heartbeat.parseFrom(req);
        } catch (InvalidProtocolBufferException ex) {
            ex.printStackTrace();
            return;
        }

        synchronized (nodeLock) {
            int nodeId = heartbeat.getNode().getId();

            // If we don't have info on the node or it has
            // restarted, start tracking it
            if (!activeNodes.containsKey(nodeId)) {
                activeNodes.put(nodeId, new DataNode(heartbeat.getNode()));
            }

            // Update most recent heartbeat time
            DataNode node = activeNodes.get(nodeId);
            node.latestHeartbeat = System.currentTimeMillis();

            synchronized (fileLock) {
                // Map all the DataNode's blocks to a flat list
                // so that we can take the union of it with the
                // existing block info stored on NameNode.
                Stream<DataNodeBlockInfo> nodeBlockInfoList = heartbeat
                        .getAvailableFileBlocksList()
                        .stream()
                        .map(fileBlock -> {
                            // Track the total number of blocks for each file.
                            // If we see a block number greater than the max known for a file,
                            // that means a Client was successfully able to write data for
                            // that block to the node.
                            fileStatuses.putIfAbsent(fileBlock.getFilename(), new FileStatus(null, 0));
                            FileStatus status = fileStatuses.get(fileBlock.getFilename());
                            status.maxBlockNumber = Long.max(status.maxBlockNumber, fileBlock.getFileBlock());

                            return new DataNodeBlockInfo(
                                    fileBlock.getFilename(),
                                    fileBlock.getFileBlock(),
                                    new DataNode(heartbeat.getNode())
                            );
                        });

                Stream<DataNodeBlockInfo> difference = blockInfoList
                        .stream()
                        .filter(blockInfo -> !blockInfo.node.equals(node));

                blockInfoList = Stream.concat(nodeBlockInfoList, difference)
                        .collect(Collectors.toSet());
            }
        }
    }

    private static byte[] createOpenCloseResponse(Operations.StatusCode status) {
        return Operations.OpenCloseResponse
                .newBuilder()
                .setStatus(status)
                .build()
                .toByteArray();
    }

    private static byte[] createGetBlockLocationsResponse(Operations.StatusCode status) {
        return createGetBlockLocationsResponse(status, new ArrayList<>());
    }

    private static byte[] createGetBlockLocationsResponse(
            Operations.StatusCode status,
            List<Operations.DataNode> nodes
    ) {
        return Operations.GetBlockLocationsResponse
                .newBuilder()
                .setStatus(status)
                .addAllNodes(nodes)
                .build()
                .toByteArray();
    }

    private static Operations.DataNode convertDataNodeToProto(DataNode node) {
        return Operations.DataNode.newBuilder()
                .setIp(node.ip)
                .setPort(node.port)
                .setId(node.id)
                .build();
    }

    private void persistState(String path) {
        synchronized (fileLock) {
            try {
                Files.createDirectories(Paths.get(path).getParent());
                OutputStream stateStream = new FileOutputStream(path);

                for (Map.Entry<String, FileStatus> entry : fileStatuses.entrySet()) {
                    String line = entry.getKey() + "," + entry.getValue().maxBlockNumber + "\n";
                    stateStream.write(line.getBytes());
                }

                stateStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, FileStatus> loadState(String path) {
        try (Stream<String> stateStream = Files.lines(Paths.get(path))) {
            return stateStream
                    .filter(line -> line.contains(","))
                    .map(line -> line.split(","))
                    .collect(Collectors.toMap(
                            line -> line[0],  // key is filename, value is maxBlockNumber
                            line -> new FileStatus(Integer.parseInt(line[1]))
                    ));
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    private void removeDeadNodes() {
        synchronized (nodeLock) {
            long currentTime = System.currentTimeMillis();

            activeNodes = activeNodes.keySet()
                    .stream()
                    .filter(id -> currentTime - activeNodes.get(id).latestHeartbeat < HEARTBEAT_THRESHOLD)
                    .collect(Collectors.toConcurrentMap(
                            Function.identity(),  // name itself is the key
                            activeNodes::get
                    ));

            System.out.println(Arrays.toString(activeNodes.values().toArray()));
        }
    }

    public static class DataNode {
        String ip;
        int port;
        int id;
        long latestHeartbeat;

        public DataNode(String addr, int p, int id) {
            this.ip = addr;
            this.port = p;
            this.id = id;
            this.latestHeartbeat = System.currentTimeMillis();
        }

        public DataNode(Operations.DataNode self) {
            this.ip = self.getIp();
            this.port = self.getPort();
            this.id = self.getId();
            this.latestHeartbeat = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DataNode && this.id == ((DataNode) obj).id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.id);
        }

        @Override
        public String toString() {
            return String.format("IDataNode-%d (%s:%d)", this.id, this.ip, this.port);
        }
    }

    /**
     * [
     * [File 1, Block 1, Node 1],
     * [File 1, Block 1, Node 2],
     * [File 1, Block 2, Node 2],
     * [File 1, Block 2, Node 3],
     * ]
     */
    public static class DataNodeBlockInfo {
        String filename;
        long blockNumber;
        DataNode node;

        DataNodeBlockInfo(String filename, long blockNumber, DataNode node) {
            this.filename = filename;
            this.blockNumber = blockNumber;
            this.node = node;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DataNodeBlockInfo))
                return false;

            DataNodeBlockInfo other = (DataNodeBlockInfo) obj;
            return this.filename.equals(other.filename)
                    && this.blockNumber == other.blockNumber
                    && this.node.equals(other.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.filename, this.blockNumber, this.node);
        }
    }

    public static class FileStatus {
        Operations.FileMode openMode;
        long maxBlockNumber;
        int openHandles;

        FileStatus(long maxBlock) {
            openMode = null;
            openHandles = 0;
            maxBlockNumber = maxBlock;
        }

        FileStatus(Operations.FileMode mode, int handles) {
            openMode = mode;
            openHandles = handles;
            maxBlockNumber = 0;
        }
    }

}
