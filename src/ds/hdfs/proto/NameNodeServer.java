package ds.hdfs.proto;

import ds.hdfs.Utils;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NameNodeServer {
    private final int port;
    private final Server server;

    public NameNodeServer() throws IOException {
        // Get NameNode's configuration
        Map<String, String> config = Utils.parseConfigFile("src/nn_config.txt");

        String description = config.get("DESCRIPTION");
        String ip = config.get("IP");
        int port = Integer.parseInt(config.get("PORT"));
        int replicationFactor = Integer.parseInt(config.get("REPLICATION_FACTOR"));

        NameNodeService nameNodeService =
                new NameNodeService(ip, port, description, replicationFactor);

        this.port = port;
        this.server = ServerBuilder
                .forPort(port)
                .addService(nameNodeService)
                .build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                NameNodeServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        NameNodeServer server = new NameNodeServer();
        server.start();
        server.blockUntilShutdown();
    }
}
