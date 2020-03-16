package ds.hdfs;

import com.google.protobuf.ByteString;
import ds.hdfs.proto.Operations;
import ds.hdfs.proto.IDataNodeGrpc;
import ds.hdfs.proto.INameNodeGrpc;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class DataNodeService extends IDataNodeGrpc.IDataNodeImplBase {

    private static final long HEARTBEAT_INTERVAL = 250;

    private String ip;
    private int id;
    private int port;
    private INameNodeGrpc.INameNodeBlockingStub nameNode;
    private long failedHeartbeats = 0;

    DataNodeService(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendHeartbeat();
                    failedHeartbeats = 0;
                } catch (IOException | StatusRuntimeException e) {
                    failedHeartbeats++;
                    System.err.printf("Failed to send heartbeat (attempt #%d)\n", failedHeartbeats);
                }
            }
        }, 0, HEARTBEAT_INTERVAL);
    }

    @Override
    public void readBlock(Operations.ReadWriteRequest request, StreamObserver<Operations.ReadWriteResponse> responseObserver) {
        String blockFileName = String.format("data/node-%d/%s.%s",
                id, request.getFilename(), request.getBlockNumber());
        File file = new File(blockFileName);

        Operations.ReadWriteResponse response;

        if(!file.exists()) {
            response = createReadWriteResponse(Operations.StatusCode.E_INVAL);
        } else {
            try {
                Path path = Paths.get(blockFileName);
                byte[] data = Files.readAllBytes(path);

                response = createReadWriteResponse(Operations.StatusCode.OK,
                        ByteString.copyFrom(data));
            } catch (IOException e) {
                e.printStackTrace();
                response = createReadWriteResponse(Operations.StatusCode.E_IO);
            }
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void writeBlock(Operations.ReadWriteRequest request, StreamObserver<Operations.ReadWriteResponse> responseObserver) {
        String fileName = request.getFilename();
        long blockNumber = request.getBlockNumber();
        byte[] contents = request.getContents().toByteArray();

        String blockFileName = String.format("data/node-%d/%s.%s",
                id, fileName, blockNumber);
        File file = new File(blockFileName);

        Operations.StatusCode status = Operations.StatusCode.OK;

        if(!file.exists()) {
            try {
                Path pathToFile = Paths.get(blockFileName);
                Files.createDirectories(pathToFile.getParent());
                Files.createFile(pathToFile);
            } catch (IOException e) {
                System.err.println("Failed to create block file");
                e.printStackTrace();
                status = Operations.StatusCode.E_IO;
            }
        }

        if(status != Operations.StatusCode.E_IO) {
            try (OutputStream outputStream = new FileOutputStream(new File(blockFileName))) {
                outputStream.write(contents);
            } catch (Exception e) {
                e.printStackTrace();
                status = Operations.StatusCode.E_IO;
            }
        }

        responseObserver.onNext(createReadWriteResponse(status));
        responseObserver.onCompleted();
    }

    private void sendHeartbeat() throws IOException, StatusRuntimeException {
        if(nameNode == null)
            nameNode = Utils.getNameNodeStub();

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

        Operations.Heartbeat heartbeat = Operations.Heartbeat
                .newBuilder()
                .setNode(Operations.DataNode
                        .newBuilder()
                        .setId(id)
                        .setIp(ip)
                        .setPort(port)
                        .build())
                .addAllAvailableFileBlocks(blocks)
                .build();

        nameNode.heartbeat(heartbeat);
    }

    private static Operations.ReadWriteResponse createReadWriteResponse(
            Operations.StatusCode status
    ) {
        return createReadWriteResponse(status, ByteString.EMPTY);
    }

    private static Operations.ReadWriteResponse createReadWriteResponse(
            Operations.StatusCode status,
            ByteString contents
    ) {
        return Operations.ReadWriteResponse
                .newBuilder()
                .setStatus(status)
                .setContents(contents)
                .build();
    }
}
