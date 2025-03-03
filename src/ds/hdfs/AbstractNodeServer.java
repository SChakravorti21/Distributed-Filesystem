package ds.hdfs;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.concurrent.TimeUnit;

public abstract class AbstractNodeServer {
    protected int port;
    protected Server server;
    BindableService service;

    void buildServer() {
        this.server = ServerBuilder
                .forPort(port)
                .addService(service)
                .build();
    }

    /** Start serving requests. */
    void start(String name) throws Exception {
        server.start();
        System.out.println(name + " server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                AbstractNodeServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));

        this.blockUntilShutdown();
    }

    /** Stop serving requests and shutdown resources. */
    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
