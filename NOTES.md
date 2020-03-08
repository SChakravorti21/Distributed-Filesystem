# Communication b/w DataNodes and NameNodes

### Heartbeats
- DataNode sends to NameNode
- Allows NameNode to track DataNodes that are alive, and what blocks they store
- DataNode creates .tmp files when writing blocks, renames to original name when done
  - Prevents race condition with sending block reports

### `readBlock`
- Client sends to DataNode

### `writeBlock`
- Client sends to DataNode

### `list`/`open`/`close`
- Client sends to NameNode

### `assignBlock`/`getBlockLocations`
- Client sends to NameNode


## Operations

### Writing a file
- Client asks NameNode `open(filename, 'w')`
  - NameNode checks if filename already exists
  - Rejects request if filename already exists
  - Return success status code otherwise
- If opening fails, inform the user
- If opening succeeds:
  - Client chunks the local file into `blockSize` chunks
  - For each chunk:
    - Ask NameNode to `assignBlock` to the DataNodes
    - For each returned DataNode, Client makes `writeBlock` request
    - Note: NameNode doesn't actually store the DataNodes persisting a block until it receives heartbeats indicating so
  - As each DataNode performs writes and sends heartbeats to NameNode, NameNode finds out that new blocks have been added to a file
    - NameNode then persists the list of filenames and how many blocks are in the file
- Client `close` file

### Reading a file
- Client asks NameNode to `open(filename, 'r')`
  - NameNode checks if the file exists and that no one else is currently **writing** to it
  - Return appropriate status code
- If opening fails, inform user
- If opening succeeds:
  - Client requests `getBlockLocations()` to NameNode
  - NameNode checks cache of which DataNodes store blocks for the file, returns to Client
  - `{ block1: [Node 1, Node 2] ... }`
  - For each block:
    - Client requests DataNodes to read block until at least one succeeds
    - Write blocks to `local_file` that was specified by user
    - If all requests to read any block *fails*, then reading entire file fails
      - Delete the `local_file`
  - If reading all blocks succeeds, then reading file succeeded

## `protoc` Definitions
- ReadWriteRequest.proto
  - req: filename, req: block number, optional: block contents
- ReadWriteResponse.proto
  - req: status, optional: block contents
- OpenCloseRequest.proto
  - req: filename, req: mode
- OpenCloseResponse.proto
  - req: status
- AssignBlockRequest.proto
  - req: filename, req: block number
- AssignBlockResponse.proto
  - req, repeated: DataNodes (Unique Identifier, IP, Port)
- GetBlockLocationsRequest.proto
  - req: filename
- GetBlockLocationsResponse.proto
  - req: map { block# : [ DataNodes ] }
- ListResponse.proto (input for list() can be made empty)
  - req, repeated: filenames
- Heartbeat.proto (DataNode -> NameNode, can make return type `void` on `INameNode`)
  - req: DataNode, req: map { filename : [ block numbers ] }