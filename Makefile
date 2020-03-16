CLASSPATH=".:build:libs/*"

clean:
	rm -rf bin/

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
	    --add-modules=java.xml.ws.annotation \
	    src/ds/hdfs/NameNodeServer.java \
	    src/ds/hdfs/DataNodeServer.java \
	    src/ds/hdfs/Client.java

run_name_node:
	java -cp ${CLASSPATH} ds.hdfs.NameNodeServer

run_data_node:
	java -cp ${CLASSPATH} ds.hdfs.DataNodeServer ${CONFIG}

run_client:
	java -cp ${CLASSPATH} ds.hdfs.Client
