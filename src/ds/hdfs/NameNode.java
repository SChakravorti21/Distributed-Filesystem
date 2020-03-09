package ds.hdfs;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NameNode implements INameNode {

    // H: Times are always in milliseconds
    // Scan for dead DataNodes every second
    private static final long SCAN_INTERVAL = 1000;
    // How old a heartbeat has to be for the DataNode
    // to be considered dead
    private static final long HEARTBEAT_THRESHOLD = 1000;

    // NameNode's properties and locks
    private String ipAddress;
    private int portNumber;
    private String identifier;
    private static final Object nodeLock = new Object();
    private static final Object fileLock = new Object();

    // Tracking active DataNodes and their properties
    private Timer timer = new Timer();
    private Map<Integer, DataNode> activeNodes = new HashMap<>();

    // Tracking file information, such what files are available,
    // whether they're being written to, and which nodes store chunks
    private Map<String, FileStatus> fileStatuses = new HashMap<>();
    private Set<DataNodeBlockInfo> blockInfoList = new HashSet<>();

    // We use the registry to contact DataNodes?
    protected Registry serverRegistry;

    public NameNode(String ipAddress, int portNumber, String name) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.identifier = name;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                synchronized (nodeLock) {
                    activeNodes = activeNodes.keySet()
                            .stream()
                            .filter(id -> currentTime - activeNodes.get(id).latestHeartbeat < HEARTBEAT_THRESHOLD)
                            .collect(Collectors.toConcurrentMap(
                                    Function.identity(),  // name itself is the key
                                    activeNodes::get
                            ));
                }
            }
        }, SCAN_INTERVAL, SCAN_INTERVAL);
    }

    public static void main(String[] args)
            throws InterruptedException, NumberFormatException, IOException {

    }

    public byte[] openFile(byte[] req) throws RemoteException {
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
                // If the file doesn't exist, it can't be opened
                status = Operations.StatusCode.E_NOENT;
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

    public byte[] closeFile(byte[] req) throws RemoteException {
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

    public byte[] getBlockLocations(byte[] inp) throws RemoteException {
        return null;
    }


    public byte[] assignBlock(byte[] inp) throws RemoteException {
        return null;
    }


    public byte[] list(byte[] inp) throws RemoteException {
        synchronized (fileLock) {
            return Operations.ListResponse
                    .newBuilder()
                    .addAllFilenames(fileStatuses.keySet())
                    .build()
                    .toByteArray();
        }
    }

    // Datanode <-> Namenode interaction methods

    public byte[] blockReport(byte[] inp) throws RemoteException {
        return null;
    }


    public void heartBeat(byte[] req) throws RemoteException {
        Operations.Heartbeat heartbeat;

        try {
            heartbeat = Operations.Heartbeat.parseFrom(req);
        } catch (InvalidProtocolBufferException ex) {
            ex.printStackTrace();
            return;
        }

        synchronized (nodeLock) {
            int nodeId = heartbeat.getNode().getId();

            // If we don't have info on the node,
            // start storing info on it
            if(!activeNodes.containsKey(nodeId)) {
                activeNodes.put(nodeId, new DataNode(heartbeat.getNode()));
            }

            // Update most recent heartbeat time
            DataNode node = activeNodes.get(nodeId);
            node.latestHeartbeat = System.currentTimeMillis();

            synchronized (fileLock) {
                // Map all the DataNode's blocks to a flat list
                // so that we can take the union of it with the
                // existing block info stored on NameNode.
                List<DataNodeBlockInfo> nodeBlockInfoList =
                        heartbeat.getAvailableFileBlocksList()
                            .stream()
                            .flatMap(fileBlocks -> fileBlocks.getFileBlocksList()
                                    .stream()
                                    .map(blockNumber -> new DataNodeBlockInfo(
                                            fileBlocks.getFilename(),
                                            blockNumber,
                                            new DataNode(heartbeat.getNode())
                                    )))
                            .collect(Collectors.toList());

                blockInfoList.addAll(nodeBlockInfoList);
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
    }

    /**
     * [
     *   [File 1, Block 1, Node 1],
     *   [File 1, Block 1, Node 2],
     *   [File 1, Block 2, Node 2],
     *   [File 1, Block 2, Node 3],
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
            if(! (obj instanceof DataNodeBlockInfo))
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
        Operations.FileMode openMode = null;
        int openHandles = 0;
    }

}
