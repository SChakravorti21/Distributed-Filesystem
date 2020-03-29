package ds.hdfs;

import java.util.Map;

public class NameNodeServer extends AbstractNodeServer{

    public NameNodeServer() throws Exception {
        // Get NameNode's configuration
        Map<String, String> config = Utils.parseConfigFile("src/nn_config.txt");

        String description = config.get("DESCRIPTION");
        String ip = config.get("IP");
        int port = Integer.parseInt(config.get("PORT"));
        int replicationFactor = Integer.parseInt(config.get("REPLICATION_FACTOR"));

        this.port = port;
        this.service = new NameNodeService(ip, port, description, replicationFactor);
    }

    public static void main(String[] args) throws Exception {
        NameNodeServer server = new NameNodeServer();
        server.buildServer();
        server.start("NameNode");
    }

}
