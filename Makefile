CLASSPATH=".:build:libs/protobuf-java-3.11.4.jar"

clean:
	rm -rf bin/

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
		src/ds/hdfs/NameNode.java \
		src/ds/hdfs/DataNode.java \
		src/ds/hdfs/Client.java

run_name_node:
	java -cp ${CLASSPATH} ds.hdfs.NameNode

run_data_node:
	java -cp ${CLASSPATH} ds.hdfs.DataNode ${CONFIG}

run_client:
	java -cp ${CLASSPATH} ds.hdfs.Client
