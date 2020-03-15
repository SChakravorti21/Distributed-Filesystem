CLASSPATH=".:build:libs/*"

clean:
	rm -rf bin/

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
	    --add-modules=java.xml.ws.annotation \
	    src/ds/hdfs/proto/NameNodeServer.java

run_name_node:
	java -cp ${CLASSPATH} ds.hdfs.proto.NameNodeServer

run_data_node:
	java -cp ${CLASSPATH} ds.hdfs.DataNode ${CONFIG}

run_client:
	java -cp ${CLASSPATH} ds.hdfs.Client
