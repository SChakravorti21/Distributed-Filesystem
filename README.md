# Distributed Filesystem (HDFS)

Group Members:
- Bliss Hu (byh5)
- Shoumyo Chakravorti (sac384)

Learnings
---------
The rest of this documentation goes into greater detail about this project (including how to run it), but at a high level here are some of the things we learned from this project:
- How to get nodes to communicate between each other
- How to stitch together multiple RPCs to provide transparent, seemingly-atomic operations for the end-user 
- How to ensure data consistency with multiple clients
  - We implemented mutual exclusion and reader-writer locking on the NameNode to help with this
- How to detect failures (using heartbeats so that the NameNode has an idea of what DataNodes are available)
- How to recover from failures (eg. if a DataNode or NameNode restarts)
  - NameNode state is persisted at `data/nn_state.txt` for seamless recovery
- How to partition data effectively to make the best use of replication
  - The NameNode shuffles the list of available DataNodes before returning it to the Client, so that all DataNodes have an equal chance of being assigned blocks
- How to gracefully handle errors (all responses contain a `StatusCode`, which are dealt with appropriately by the Client)

### Division of Work
Both members worked equally on this project. Shoumyo worked on the NameNode, Bliss worked on the DataNode, and both split up work on the Client.

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

#### Compiling the Code
Before being able to run the HDFS code, you must first compile the code. In order to do this, use the command `make build` inside the project's root directory. Our project compiles for the Java 10 target, so your compiler must support Java 10.

#### Dependencies
We implemented RPC using `gRPC` rather than Java RMI, so you will see a number of `.jar` files in our `libs` folder which facilitate in using gRPC. This is in addition to the `protobuf` jar required to serialize/deserialize Protocol Buffers. 

### Running the NameNode
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

### Running DataNodes
Prior to running the DataNode, ensure that `src/nn_config.txt` contains the accurate configuration 
information of the NameNode, as the DataNode requires this to contact the NameNode.

The configuration for DataNodes can be stored anywhere as a `.txt` file. An example configuration
file is shown below (comments are not and should not be included in the actual config file):

```
ID=1                        // Unique integer ID for each DataNode
IP=less.cs.rutgers.edu      // Host or IP address of DataNode
PORT=2006                   // Port on which the DataNode is made available
```

In order to run a DataNode, invoke the following command from the project's root directory: `make run_data_node`.
By default, this will read the configuration details from `src/dn_config.txt`. You can specify a different
path for the configuration file like so: `make run_data_node CONFIG=src/dn_config2.txt`.

### Running the Client
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

### RPC - **gRPC**
The assignment suggested to use Java RMI if implementing the project in Java. However, we found RMI to be an restrictive and bloated solution for this project:
1. We came across a number of issues trying to deploy earlier implementations of your project using RMI. In particular, RMI's registry semantics forced us to programmatically generate registries for every stub since we did not have access to the `rmiregistry` Linux command on iLabs and the lifecycle of programmatically-created registries was tied to the host (DataNode) process. This practice surely goes against the essence of a nameserver.
2. Bringing down the NameNode would requires DataNodes to manually reconnect to it by acquiring the NameNode stub repeatedly. The same goes for Clients trying to connect to DataNodes.
3. RMI already has built-in support for serialization of Java objects. Sending protobufs as byte arrays through RMI requests obscures the contract of these methods at the interface level.
4. The true power of RMI comes in being able to pass around instances of remote objects just like any other Java object. Since our simple filesystem only acquires stubs when needed, this does not harness the benefits of RMI and even introduces unnecessary roundtrips in communication (one roundtrip to acquire a stub, and another roundtrip to actually make the request).

Because of the above reasons, we opted to use a solution better suited for this project: **gRPC**. gRPC provides all the functionality we need, while leveraging the protobuf definitions we had already written. gRPC also allowed our services to be more expressive about their inputs and outputs using the compiled protobuf messages as parameters and return types. Furthermore, gRPC allowed us to simply designate a NameNode/DataNode as running on a particular port - no registry nonsense. Lastly, gRPC eliminated unnecesary roundtrips in making requests, reducing latency and notably improving performance for large files (~1GB).

You can find the definitions for our gRPC services in `src/ds/hdfs/hdfs_services.proto`.

### Thread Synchronization
Since a distributed filesystem like this must naturally be multithreaded to support the operation of several DataNodes and potentially multiple Clients, synchronization is of utmost importance to avoid data corruption and transparent operation of the filesystem. Since critical, non-idempotent functionality is generally guarded through functionality exposed by the NameNode (opening/closing files, assigning blocks, sending heartbeats), a convenient solution to achieving synchronization is to simply implement synchronization at the NameNode level. By default, then, non-idempotent functionality, which is usually initiated by a request to the NameNode, is protected through mutual exclusion tactics. Specifically, the NameNode has two locks (`nodeLock` and `fileLock`) which are used to ensure exclusive access to information in RPC requests (node status and file/block data respectively).

### Heartbeats

In order for the NameNode to know what DataNodes are available, DataNodes send the NameNode heartbeats at a regular interval of 250ms. In each heartbeat, the DataNode sends information that helps Clients connect to them (IP, port, etc). Additionally, heartbeats include the list of blocks stored on the DataNode (more granular than simply filenames). This list of blocks is derived directly from the files stored in the native filesystem, i.e. the DataNode does not cache (in memory) the list of blocks it's storing, and instead simply reads from its corresponding `data/` directory to find out what blocks it's storing. Since this data is sent in heartbeats, and we saw no impact on performance even with thousands of blocks on DataNodes, we have *opted out* of the `blockReport` interaction between NameNodes and DataNodes. 

On the NameNode, much work is done to track active DataNodes and the file blocks they store. For one, the NameNode stores a list of `activeNodes` mapped by their IDs. At a regular interval of 1 second, the NameNode scans through the list of `activeNodes` and removes any node which it has not received a heartbeat from in the past 1 second. Furthermore, the NameNode aggregates block reports from all DataNodes' heartbeats into a singular flat list `blockInfoList`, where each entry stores a filename, block number, and DataNode information. This provides a convenient structure to query for `getBlockLocations` requests.

#### Restarts

Of course, it is important for a distributed filesystem (and any distributed system for that matter) to be fault tolerant. Both our DataNode and NameNode can restart and restore their previous state quite easily.

The DataNode actually does not even have to "restore" any state upon restarts. Since it always reads directly from the native filesystem to find out what blocks it's storing, the DataNode can start sending heartbeats to the NameNode immediately. This indicates to the NameNode that the DataNode has become active again, and the NameNode can continue to get that specific DataNode involved in Client interactions.

The NameNode, upon restarting, reads from `data/nn_state.txt` to find out what files were stored in the system prior to it going down (`nn_state.txt` also stores the total number of blocks and creation time for all files). As soon as the NameNode is back up, it will start receiving heartbeats from DataNodes (assuming only the NameNode went down and the DataNodes were still up). These heartbeats will indicate to the NameNode which DataNodes store which file blocks, and the NameNode can resume servicing Client requests.

### Reading/Writing Files
For the main functionality of reading and writing files in our system, we've mostly followed the guidelines of the project description and FAQ with a few tweeks:
- We've implemented reader-writer locking for accessing files. This means that writing to a file acquires an exclusive lock on that file, whereas multiple clients can read from the same file concurrently (assuming it's not being written to). This functionality made sense to us because allowing multiple clients to *write* to the same file would inevitably lead to corruption of data.
- For writing to a file, the Client only makes a single `assignBlocks` request as a performance optimization.