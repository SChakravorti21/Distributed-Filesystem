package ds.hdfs.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: hdfs_services.proto")
public final class INameNodeGrpc {

  private INameNodeGrpc() {}

  public static final String SERVICE_NAME = "hdfs.INameNode";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest,
      ds.hdfs.Operations.OpenCloseResponse> getOpenFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "openFile",
      requestType = ds.hdfs.Operations.OpenCloseRequest.class,
      responseType = ds.hdfs.Operations.OpenCloseResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest,
      ds.hdfs.Operations.OpenCloseResponse> getOpenFileMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest, ds.hdfs.Operations.OpenCloseResponse> getOpenFileMethod;
    if ((getOpenFileMethod = INameNodeGrpc.getOpenFileMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getOpenFileMethod = INameNodeGrpc.getOpenFileMethod) == null) {
          INameNodeGrpc.getOpenFileMethod = getOpenFileMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.OpenCloseRequest, ds.hdfs.Operations.OpenCloseResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "openFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.OpenCloseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.OpenCloseResponse.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("openFile"))
              .build();
        }
      }
    }
    return getOpenFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest,
      ds.hdfs.Operations.OpenCloseResponse> getCloseFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "closeFile",
      requestType = ds.hdfs.Operations.OpenCloseRequest.class,
      responseType = ds.hdfs.Operations.OpenCloseResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest,
      ds.hdfs.Operations.OpenCloseResponse> getCloseFileMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.OpenCloseRequest, ds.hdfs.Operations.OpenCloseResponse> getCloseFileMethod;
    if ((getCloseFileMethod = INameNodeGrpc.getCloseFileMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getCloseFileMethod = INameNodeGrpc.getCloseFileMethod) == null) {
          INameNodeGrpc.getCloseFileMethod = getCloseFileMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.OpenCloseRequest, ds.hdfs.Operations.OpenCloseResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "closeFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.OpenCloseRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.OpenCloseResponse.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("closeFile"))
              .build();
        }
      }
    }
    return getCloseFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.GetBlockLocationsRequest,
      ds.hdfs.Operations.GetBlockLocationsResponse> getGetBlockLocationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getBlockLocations",
      requestType = ds.hdfs.Operations.GetBlockLocationsRequest.class,
      responseType = ds.hdfs.Operations.GetBlockLocationsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.GetBlockLocationsRequest,
      ds.hdfs.Operations.GetBlockLocationsResponse> getGetBlockLocationsMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.GetBlockLocationsRequest, ds.hdfs.Operations.GetBlockLocationsResponse> getGetBlockLocationsMethod;
    if ((getGetBlockLocationsMethod = INameNodeGrpc.getGetBlockLocationsMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getGetBlockLocationsMethod = INameNodeGrpc.getGetBlockLocationsMethod) == null) {
          INameNodeGrpc.getGetBlockLocationsMethod = getGetBlockLocationsMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.GetBlockLocationsRequest, ds.hdfs.Operations.GetBlockLocationsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getBlockLocations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.GetBlockLocationsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.GetBlockLocationsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("getBlockLocations"))
              .build();
        }
      }
    }
    return getGetBlockLocationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.AssignBlockRequest,
      ds.hdfs.Operations.AssignBlockResponse> getAssignBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "assignBlock",
      requestType = ds.hdfs.Operations.AssignBlockRequest.class,
      responseType = ds.hdfs.Operations.AssignBlockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.AssignBlockRequest,
      ds.hdfs.Operations.AssignBlockResponse> getAssignBlockMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.AssignBlockRequest, ds.hdfs.Operations.AssignBlockResponse> getAssignBlockMethod;
    if ((getAssignBlockMethod = INameNodeGrpc.getAssignBlockMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getAssignBlockMethod = INameNodeGrpc.getAssignBlockMethod) == null) {
          INameNodeGrpc.getAssignBlockMethod = getAssignBlockMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.AssignBlockRequest, ds.hdfs.Operations.AssignBlockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "assignBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.AssignBlockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.AssignBlockResponse.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("assignBlock"))
              .build();
        }
      }
    }
    return getAssignBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ds.hdfs.Operations.ListResponse> getListMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "list",
      requestType = com.google.protobuf.Empty.class,
      responseType = ds.hdfs.Operations.ListResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ds.hdfs.Operations.ListResponse> getListMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, ds.hdfs.Operations.ListResponse> getListMethod;
    if ((getListMethod = INameNodeGrpc.getListMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getListMethod = INameNodeGrpc.getListMethod) == null) {
          INameNodeGrpc.getListMethod = getListMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, ds.hdfs.Operations.ListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "list"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.ListResponse.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("list"))
              .build();
        }
      }
    }
    return getListMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.Heartbeat,
      com.google.protobuf.Empty> getHeartbeatMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "heartbeat",
      requestType = ds.hdfs.Operations.Heartbeat.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.Heartbeat,
      com.google.protobuf.Empty> getHeartbeatMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.Heartbeat, com.google.protobuf.Empty> getHeartbeatMethod;
    if ((getHeartbeatMethod = INameNodeGrpc.getHeartbeatMethod) == null) {
      synchronized (INameNodeGrpc.class) {
        if ((getHeartbeatMethod = INameNodeGrpc.getHeartbeatMethod) == null) {
          INameNodeGrpc.getHeartbeatMethod = getHeartbeatMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.Heartbeat, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "heartbeat"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.Heartbeat.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new INameNodeMethodDescriptorSupplier("heartbeat"))
              .build();
        }
      }
    }
    return getHeartbeatMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static INameNodeStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<INameNodeStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<INameNodeStub>() {
        @java.lang.Override
        public INameNodeStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new INameNodeStub(channel, callOptions);
        }
      };
    return INameNodeStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static INameNodeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<INameNodeBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<INameNodeBlockingStub>() {
        @java.lang.Override
        public INameNodeBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new INameNodeBlockingStub(channel, callOptions);
        }
      };
    return INameNodeBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static INameNodeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<INameNodeFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<INameNodeFutureStub>() {
        @java.lang.Override
        public INameNodeFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new INameNodeFutureStub(channel, callOptions);
        }
      };
    return INameNodeFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class INameNodeImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Allows a client to open a file for reading or writing
     * </pre>
     */
    public void openFile(ds.hdfs.Operations.OpenCloseRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getOpenFileMethod(), responseObserver);
    }

    /**
     * <pre>
     * Allows client to indicate to NameNode that it is done
     * reading or writing
     * </pre>
     */
    public void closeFile(ds.hdfs.Operations.OpenCloseRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCloseFileMethod(), responseObserver);
    }

    /**
     * <pre>
     * Allows a client to query which DataNodes store a
     * given file block
     * </pre>
     */
    public void getBlockLocations(ds.hdfs.Operations.GetBlockLocationsRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.GetBlockLocationsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetBlockLocationsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Used by client to designate which DataNodes should store
     * a particular file block
     * </pre>
     */
    public void assignBlock(ds.hdfs.Operations.AssignBlockRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.AssignBlockResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAssignBlockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lists the files stored in HDFS (regardless of availability)
     * </pre>
     */
    public void list(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getListMethod(), responseObserver);
    }

    /**
     * <pre>
     * For DataNode to send NameNode heartbeats
     * </pre>
     */
    public void heartbeat(ds.hdfs.Operations.Heartbeat request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getHeartbeatMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getOpenFileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.OpenCloseRequest,
                ds.hdfs.Operations.OpenCloseResponse>(
                  this, METHODID_OPEN_FILE)))
          .addMethod(
            getCloseFileMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.OpenCloseRequest,
                ds.hdfs.Operations.OpenCloseResponse>(
                  this, METHODID_CLOSE_FILE)))
          .addMethod(
            getGetBlockLocationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.GetBlockLocationsRequest,
                ds.hdfs.Operations.GetBlockLocationsResponse>(
                  this, METHODID_GET_BLOCK_LOCATIONS)))
          .addMethod(
            getAssignBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.AssignBlockRequest,
                ds.hdfs.Operations.AssignBlockResponse>(
                  this, METHODID_ASSIGN_BLOCK)))
          .addMethod(
            getListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                ds.hdfs.Operations.ListResponse>(
                  this, METHODID_LIST)))
          .addMethod(
            getHeartbeatMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.Heartbeat,
                com.google.protobuf.Empty>(
                  this, METHODID_HEARTBEAT)))
          .build();
    }
  }

  /**
   */
  public static final class INameNodeStub extends io.grpc.stub.AbstractAsyncStub<INameNodeStub> {
    private INameNodeStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected INameNodeStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new INameNodeStub(channel, callOptions);
    }

    /**
     * <pre>
     * Allows a client to open a file for reading or writing
     * </pre>
     */
    public void openFile(ds.hdfs.Operations.OpenCloseRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getOpenFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Allows client to indicate to NameNode that it is done
     * reading or writing
     * </pre>
     */
    public void closeFile(ds.hdfs.Operations.OpenCloseRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCloseFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Allows a client to query which DataNodes store a
     * given file block
     * </pre>
     */
    public void getBlockLocations(ds.hdfs.Operations.GetBlockLocationsRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.GetBlockLocationsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetBlockLocationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Used by client to designate which DataNodes should store
     * a particular file block
     * </pre>
     */
    public void assignBlock(ds.hdfs.Operations.AssignBlockRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.AssignBlockResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAssignBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lists the files stored in HDFS (regardless of availability)
     * </pre>
     */
    public void list(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * For DataNode to send NameNode heartbeats
     * </pre>
     */
    public void heartbeat(ds.hdfs.Operations.Heartbeat request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getHeartbeatMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class INameNodeBlockingStub extends io.grpc.stub.AbstractBlockingStub<INameNodeBlockingStub> {
    private INameNodeBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected INameNodeBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new INameNodeBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Allows a client to open a file for reading or writing
     * </pre>
     */
    public ds.hdfs.Operations.OpenCloseResponse openFile(ds.hdfs.Operations.OpenCloseRequest request) {
      return blockingUnaryCall(
          getChannel(), getOpenFileMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Allows client to indicate to NameNode that it is done
     * reading or writing
     * </pre>
     */
    public ds.hdfs.Operations.OpenCloseResponse closeFile(ds.hdfs.Operations.OpenCloseRequest request) {
      return blockingUnaryCall(
          getChannel(), getCloseFileMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Allows a client to query which DataNodes store a
     * given file block
     * </pre>
     */
    public ds.hdfs.Operations.GetBlockLocationsResponse getBlockLocations(ds.hdfs.Operations.GetBlockLocationsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetBlockLocationsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Used by client to designate which DataNodes should store
     * a particular file block
     * </pre>
     */
    public ds.hdfs.Operations.AssignBlockResponse assignBlock(ds.hdfs.Operations.AssignBlockRequest request) {
      return blockingUnaryCall(
          getChannel(), getAssignBlockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lists the files stored in HDFS (regardless of availability)
     * </pre>
     */
    public ds.hdfs.Operations.ListResponse list(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getListMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * For DataNode to send NameNode heartbeats
     * </pre>
     */
    public com.google.protobuf.Empty heartbeat(ds.hdfs.Operations.Heartbeat request) {
      return blockingUnaryCall(
          getChannel(), getHeartbeatMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class INameNodeFutureStub extends io.grpc.stub.AbstractFutureStub<INameNodeFutureStub> {
    private INameNodeFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected INameNodeFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new INameNodeFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Allows a client to open a file for reading or writing
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.OpenCloseResponse> openFile(
        ds.hdfs.Operations.OpenCloseRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getOpenFileMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Allows client to indicate to NameNode that it is done
     * reading or writing
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.OpenCloseResponse> closeFile(
        ds.hdfs.Operations.OpenCloseRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCloseFileMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Allows a client to query which DataNodes store a
     * given file block
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.GetBlockLocationsResponse> getBlockLocations(
        ds.hdfs.Operations.GetBlockLocationsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetBlockLocationsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Used by client to designate which DataNodes should store
     * a particular file block
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.AssignBlockResponse> assignBlock(
        ds.hdfs.Operations.AssignBlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAssignBlockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lists the files stored in HDFS (regardless of availability)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.ListResponse> list(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getListMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * For DataNode to send NameNode heartbeats
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> heartbeat(
        ds.hdfs.Operations.Heartbeat request) {
      return futureUnaryCall(
          getChannel().newCall(getHeartbeatMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_OPEN_FILE = 0;
  private static final int METHODID_CLOSE_FILE = 1;
  private static final int METHODID_GET_BLOCK_LOCATIONS = 2;
  private static final int METHODID_ASSIGN_BLOCK = 3;
  private static final int METHODID_LIST = 4;
  private static final int METHODID_HEARTBEAT = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final INameNodeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(INameNodeImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_OPEN_FILE:
          serviceImpl.openFile((ds.hdfs.Operations.OpenCloseRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse>) responseObserver);
          break;
        case METHODID_CLOSE_FILE:
          serviceImpl.closeFile((ds.hdfs.Operations.OpenCloseRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.OpenCloseResponse>) responseObserver);
          break;
        case METHODID_GET_BLOCK_LOCATIONS:
          serviceImpl.getBlockLocations((ds.hdfs.Operations.GetBlockLocationsRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.GetBlockLocationsResponse>) responseObserver);
          break;
        case METHODID_ASSIGN_BLOCK:
          serviceImpl.assignBlock((ds.hdfs.Operations.AssignBlockRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.AssignBlockResponse>) responseObserver);
          break;
        case METHODID_LIST:
          serviceImpl.list((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.ListResponse>) responseObserver);
          break;
        case METHODID_HEARTBEAT:
          serviceImpl.heartbeat((ds.hdfs.Operations.Heartbeat) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class INameNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    INameNodeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ds.hdfs.proto.HdfsServices.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("INameNode");
    }
  }

  private static final class INameNodeFileDescriptorSupplier
      extends INameNodeBaseDescriptorSupplier {
    INameNodeFileDescriptorSupplier() {}
  }

  private static final class INameNodeMethodDescriptorSupplier
      extends INameNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    INameNodeMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (INameNodeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new INameNodeFileDescriptorSupplier())
              .addMethod(getOpenFileMethod())
              .addMethod(getCloseFileMethod())
              .addMethod(getGetBlockLocationsMethod())
              .addMethod(getAssignBlockMethod())
              .addMethod(getListMethod())
              .addMethod(getHeartbeatMethod())
              .build();
        }
      }
    }
    return result;
  }
}
