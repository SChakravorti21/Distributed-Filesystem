# Distributed Filesystem (HDFS)

Group Members:
- Bliss Hu (byh5)
- Shoumyo Chakravorti (sac384)

Running the Filesystem
----------------------

There are three components that need to be run in order to use the filesystem:
- **NameNode**: our system assumes at most one NameNode will be running at a given time
- **DataNodes**: these nodes store file blocks
- **Client**: the client communicates with the NameNode and DataNodes to upload/download/list files

The order in which these components are run does not matter, as nodes/client automatically reconnect 
to the necessary peers. For example, you can start the Client or DataNodes before the NameNode, and the
Client/DataNodes will automatically connect to the NameNode once the latter is brought up.

For each node type, we explain how to configure and run the node, as well as any assumptions of the node.
If you look through the configuration files, you will notice that we tested our system on a number of
iLab machines (NameNode on `kill`, DataNodes on `less` and `ls`, Client on `cp`), and we know for sure
that our system works with the provided configuration on the iLabs.

**NOTE**: for the NameNode and DataNodes, all persisted state is stored in the `data/` directory, so
*NEVER* alter the contents of this `data/` directory.

### Compiling the Code
Before being able to run the HDFS code, you must first compile the code. In order to do this, use the command `make build` inside the project's root directory. Our project compiles for the Java 10 target, so your compiler must support Java 10.

### Dependencies
We implemented RPC using `gRPC` rather than Java RMI, so you will see a number of `.jar` files in our `libs` folder which facilitate in using gRPC. This is in addition to the `protobuf` jar required to serialize/deserialize Protocol Buffers. 

## Running the NameNode
As mentioned, our system assumes at most one NameNode will be running at any time. The configuration
for the NameNode *MUST* be saved at the path `src/nn_config.txt`, and looks like the following
(comments are not and should not be included in the actual config file):

```
DESCRIPTION=NN              // The name associated with the NameNode
IP=kill.cs.rutgers.edu      // The host on which the NameNode is run
PORT=2004                   // The port on which the NameNode is run
REPLICATION_FACTOR=3        // The number of times to replicate blocks
```

Prior to running the NameNode, especially ensure that the NameNode's IP and port are set correctly.
This is necessary because the DataNodes and Clients also rely on reading from `src/nn_config.txt` to
figure out how to contact the NameNode.

In order to run the NameNode, invoke the following command from the project's root directory: `make run_name_node`.
To stop the NameNode, you can kill it as you would any other process: `Ctrl+C`.

Again, please make sure to include and update the NameNode config on machines that will be running
DataNodes and/or Clients, regardless of whether the NameNode will be run on the same machine.

## Running DataNodes
Prior to running the DataNode, ensure that `src/nn_config.txt` contains the accurate configuration 
information of the NameNode, as the DataNode requires this to contact the NameNode.

The configuration for DataNodes can be stored anywhere as a `.txt` file. An example configuration
file is shown below (comments are not and should not be included in the actual config file):

```
ID=1                        // Unique ID for each DataNode
IP=less.cs.rutgers.edu      // Host or IP address of DataNode
PORT=2006                   // Port on which the DataNode is made available
```

In order to run a DataNode, invoke the following command from the project's root directory: `make run_data_node`.
By default, this will read the configuration details from `src/dn_config.txt`. You can specify a different
path for the configuration file like so: `make run_data_node CONFIG=src/dn_config2.txt`.

## Running the Client
You can run the Client by invoking the following command: `make run_client`.
On top of this, the client supplies the basic semantics as specified in the assignment with commands: `list`, `get`, and `put`. 

The Client requires `src/nn_config.txt` to figure out the host and port of the NameNode. You also have the ability to specify block size in the client's configuration at `src/cn_config.txt` which looks like the following (comments are not and should not be included in the actual config file):

```
BLOCK_SIZE=64     // Specify block size to be 64 bytes
```

This is the only configuration which is available within the `src/cn_config.txt` file. The Client config *MUST* be stored at `src/cn_config.txt`, as there is no way to specify a different path to the config file. 

Below is an example run through of the client. In the example, we only specify our local filename when running both the `put` and `get` operations.

```
$> list  // There are no files in HDFS yet, so it outputs nothing
$> put NOTES.md
Going to write local file (NOTES.md) to remote file (NOTES.md)
$> list
NOTES.md
$> get NOTES.md  // Note that this will overwrite the local copy of NOTES.md
Going to read remote file (NOTES.md) to local file (NOTES.md)
$> quit
```

Below is another example run of the client. You can see we've added some extra functionality which is able to rename your file when putting or getting from the filesystem.

```
$> list
$> put NOTES.md NOTES.hdfs.md           // put <local filepath> <hdfs filename>
Going to write local file (NOTES.md) to remote file (NOTES.hdfs.md)
$> list                                 // list hdfs filenames
NOTES.hdfs.md
$> get NOTES.hdfs.md NOTES.read.md      // get <hdfs filename> <local filename>
Going to read remote file (NOTES.hdfs.md) to local file (NOTES.read.md)
```

Implementation
--------------
From a high level, the client is able to request metadata information from the NameNode for get, put, and list and is then able to query each individual DataNode for data. Afterwards, the client itself is able to stitch together the queried data for the user into a single local file.

- we dont use blockReport because already do that in heartbeat
- performance optimization: assignblock
- gRPC instead of RMI, and why
- error-handling (StatusCode)
- reader-writer locking
- synchronization on NameNode (and why it's not necessary on Client/DataNode)

## Division of Work
Both members worked equally on this project. Shoumyo worked on the NameNode, Bliss worked on the DataNode, and both split up work on the Client. 

Learnings
---------
We learned a lot about the difficulties of building a distributed system through this project. Some of the problems we faced were:
- How to get nodes to communicate between each other
- How to ensure data consistency with multiple clients
  - We implemented mutual exclusion and reader-writer locking on the NameNode to help with this
- How to detect failures (using heartbeats so that the NameNode has an idea of what DataNodes are available)
- How to recover from failures (eg. if a DataNode or NameNode restarts)
  - NameNode state is persisted at `data/nn_state.txt` for seamless recovery
- How to partition data effectively to make the best of use replication
  - The NameNode shuffles the list of available DataNodes before returning it to the Client, so that all DataNodes have an equal chance of being assigned blocks
- How to gracefully handle errors (all responses contain status codes and are dealt with appropriately by the Client)

