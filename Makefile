CLASSPATH=".:build:libs/*"
CONFIG="src/dn_config.txt"

clean:
	rm -rf build/

clear_blocks:
	rm -rf data/node-*

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
	    --release=10 --add-modules=java.xml.ws.annotation \
	    src/ds/hdfs/NameNodeServer.java \
	    src/ds/hdfs/DataNodeServer.java \
	    src/ds/hdfs/Client.java

run_name_node:
	java -cp ${CLASSPATH} ds.hdfs.NameNodeServer

run_data_node:
	java -cp ${CLASSPATH} ds.hdfs.DataNodeServer ${CONFIG}

run_client:
	java -cp ${CLASSPATH} ds.hdfs.Client
