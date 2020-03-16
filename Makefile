CLASSPATH=".:build:libs/*"

clean:
	rm -rf bin/

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
	    --add-modules=java.xml.ws.annotation \
	    src/ds/hdfs/proto/NameNodeServer.java \
	    src/ds/hdfs/proto/DataNodeServer.java \
	    src/ds/hdfs/proto/GrpcClient.java

run_name_node:
	java -cp ${CLASSPATH} ds.hdfs.proto.NameNodeServer

run_data_node:
	java -cp ${CLASSPATH} ds.hdfs.proto.DataNodeServer ${CONFIG}

run_client:
	java -cp ${CLASSPATH} ds.hdfs.proto.GrpcClient
