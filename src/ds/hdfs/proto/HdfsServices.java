// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: hdfs_services.proto

package ds.hdfs.proto;

public final class HdfsServices {
  private HdfsServices() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023hdfs_services.proto\022\004hdfs\032\nhdfs.proto\032" +
      "\033google/protobuf/empty.proto2\234\003\n\017NameNod" +
      "eService\022=\n\010openFile\022\026.hdfs.OpenCloseReq" +
      "uest\032\027.hdfs.OpenCloseResponse\"\000\022>\n\tclose" +
      "File\022\026.hdfs.OpenCloseRequest\032\027.hdfs.Open" +
      "CloseResponse\"\000\022V\n\021getBlockLocations\022\036.h" +
      "dfs.GetBlockLocationsRequest\032\037.hdfs.GetB" +
      "lockLocationsResponse\"\000\022D\n\013assignBlock\022\030" +
      ".hdfs.AssignBlockRequest\032\031.hdfs.AssignBl" +
      "ockResponse\"\000\0224\n\004list\022\026.google.protobuf." +
      "Empty\032\022.hdfs.ListResponse\"\000\0226\n\theartbeat" +
      "\022\017.hdfs.Heartbeat\032\026.google.protobuf.Empt" +
      "y\"\0002\222\001\n\017DataNodeService\022>\n\treadBlock\022\026.h" +
      "dfs.ReadWriteRequest\032\027.hdfs.ReadWriteRes" +
      "ponse\"\000\022?\n\nwriteBlock\022\026.hdfs.ReadWriteRe" +
      "quest\032\027.hdfs.ReadWriteResponse\"\000B\021\n\rds.h" +
      "dfs.protoP\001"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          Operations.getDescriptor(),
          com.google.protobuf.EmptyProto.getDescriptor(),
        });
    Operations.getDescriptor();
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
