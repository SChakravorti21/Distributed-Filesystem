package ds.hdfs.proto;

import ds.hdfs.Utils;

import java.util.Map;

public class DataNodeServer extends AbstractNodeServer {

    private DataNodeServer(String configFile) throws Exception {
        Map<String, String> config = Utils.parseConfigFile(configFile);
        int id = Integer.parseInt(config.get("ID"));
        String ip = config.get("IP");
        int stubPort = Integer.parseInt(config.get("PORT"));

        this.port = stubPort;
        this.service = new DataNodeService(id, ip, stubPort);
    }

    public static void main(String[] args) throws Exception {
        DataNodeServer server = new DataNodeServer(args[0]);
        server.buildServer();
        server.start("DataNode");
    }

}
