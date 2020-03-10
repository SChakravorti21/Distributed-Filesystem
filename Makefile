CLASSPATH=".:build:libs/protobuf-java-3.11.4.jar"

clean:
	rm -rf bin/

build: clean
	javac -d build -g -sourcepath src -cp $(CLASSPATH) \
		src/ds/hdfs/NameNode.java

run_name_node: build
	java -cp ${CLASSPATH} ds.hdfs.NameNode
