package ds.hdfs;

import ds.hdfs.proto.IDataNodeGrpc;
import ds.hdfs.proto.INameNodeGrpc;
import ds.hdfs.proto.NameNodeService;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    public static Map<String, String> parseConfigFile(String filename) throws IOException {
        return Files.lines(Paths.get(filename))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(
                        line -> line[0],
                        line -> line[1]
                ));
    }

    public static INameNode connectNameNode() throws IOException, NotBoundException {
        String registryHost = Utils.parseConfigFile("src/nn_config.txt").get("IP");
        Registry serverRegistry = LocateRegistry.getRegistry(registryHost, NameNode.REGISTRY_PORT);
        return (INameNode) serverRegistry.lookup("INameNode");
    }

    public static INameNodeGrpc.INameNodeBlockingStub getNameNodeStub() throws IOException {
        Map<String, String> nameNodeConfig = Utils.parseConfigFile("src/nn_config.txt");
        String host = nameNodeConfig.get("IP");
        int port = Integer.parseInt(nameNodeConfig.get("PORT"));

        return INameNodeGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build());
    }

    public static IDataNodeGrpc.IDataNodeBlockingStub getDataNodeStub(String ip, int port) {
        return IDataNodeGrpc.newBlockingStub(ManagedChannelBuilder
            .forAddress(ip, port)
            .usePlaintext()
            .build());
    }

}
