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
public final class IDataNodeGrpc {

  private IDataNodeGrpc() {}

  public static final String SERVICE_NAME = "hdfs.IDataNode";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest,
      ds.hdfs.Operations.ReadWriteResponse> getReadBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "readBlock",
      requestType = ds.hdfs.Operations.ReadWriteRequest.class,
      responseType = ds.hdfs.Operations.ReadWriteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest,
      ds.hdfs.Operations.ReadWriteResponse> getReadBlockMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest, ds.hdfs.Operations.ReadWriteResponse> getReadBlockMethod;
    if ((getReadBlockMethod = IDataNodeGrpc.getReadBlockMethod) == null) {
      synchronized (IDataNodeGrpc.class) {
        if ((getReadBlockMethod = IDataNodeGrpc.getReadBlockMethod) == null) {
          IDataNodeGrpc.getReadBlockMethod = getReadBlockMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.ReadWriteRequest, ds.hdfs.Operations.ReadWriteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "readBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.ReadWriteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.ReadWriteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IDataNodeMethodDescriptorSupplier("readBlock"))
              .build();
        }
      }
    }
    return getReadBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest,
      ds.hdfs.Operations.ReadWriteResponse> getWriteBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "writeBlock",
      requestType = ds.hdfs.Operations.ReadWriteRequest.class,
      responseType = ds.hdfs.Operations.ReadWriteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest,
      ds.hdfs.Operations.ReadWriteResponse> getWriteBlockMethod() {
    io.grpc.MethodDescriptor<ds.hdfs.Operations.ReadWriteRequest, ds.hdfs.Operations.ReadWriteResponse> getWriteBlockMethod;
    if ((getWriteBlockMethod = IDataNodeGrpc.getWriteBlockMethod) == null) {
      synchronized (IDataNodeGrpc.class) {
        if ((getWriteBlockMethod = IDataNodeGrpc.getWriteBlockMethod) == null) {
          IDataNodeGrpc.getWriteBlockMethod = getWriteBlockMethod =
              io.grpc.MethodDescriptor.<ds.hdfs.Operations.ReadWriteRequest, ds.hdfs.Operations.ReadWriteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "writeBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.ReadWriteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ds.hdfs.Operations.ReadWriteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IDataNodeMethodDescriptorSupplier("writeBlock"))
              .build();
        }
      }
    }
    return getWriteBlockMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static IDataNodeStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IDataNodeStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IDataNodeStub>() {
        @java.lang.Override
        public IDataNodeStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IDataNodeStub(channel, callOptions);
        }
      };
    return IDataNodeStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static IDataNodeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IDataNodeBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IDataNodeBlockingStub>() {
        @java.lang.Override
        public IDataNodeBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IDataNodeBlockingStub(channel, callOptions);
        }
      };
    return IDataNodeBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static IDataNodeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IDataNodeFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IDataNodeFutureStub>() {
        @java.lang.Override
        public IDataNodeFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IDataNodeFutureStub(channel, callOptions);
        }
      };
    return IDataNodeFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class IDataNodeImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * For client to read blocks from DataNode
     * </pre>
     */
    public void readBlock(ds.hdfs.Operations.ReadWriteRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getReadBlockMethod(), responseObserver);
    }

    /**
     * <pre>
     * For client to write blocks to DataNode
     * </pre>
     */
    public void writeBlock(ds.hdfs.Operations.ReadWriteRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getWriteBlockMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getReadBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.ReadWriteRequest,
                ds.hdfs.Operations.ReadWriteResponse>(
                  this, METHODID_READ_BLOCK)))
          .addMethod(
            getWriteBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ds.hdfs.Operations.ReadWriteRequest,
                ds.hdfs.Operations.ReadWriteResponse>(
                  this, METHODID_WRITE_BLOCK)))
          .build();
    }
  }

  /**
   */
  public static final class IDataNodeStub extends io.grpc.stub.AbstractAsyncStub<IDataNodeStub> {
    private IDataNodeStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IDataNodeStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IDataNodeStub(channel, callOptions);
    }

    /**
     * <pre>
     * For client to read blocks from DataNode
     * </pre>
     */
    public void readBlock(ds.hdfs.Operations.ReadWriteRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getReadBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * For client to write blocks to DataNode
     * </pre>
     */
    public void writeBlock(ds.hdfs.Operations.ReadWriteRequest request,
        io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getWriteBlockMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class IDataNodeBlockingStub extends io.grpc.stub.AbstractBlockingStub<IDataNodeBlockingStub> {
    private IDataNodeBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IDataNodeBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IDataNodeBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * For client to read blocks from DataNode
     * </pre>
     */
    public ds.hdfs.Operations.ReadWriteResponse readBlock(ds.hdfs.Operations.ReadWriteRequest request) {
      return blockingUnaryCall(
          getChannel(), getReadBlockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * For client to write blocks to DataNode
     * </pre>
     */
    public ds.hdfs.Operations.ReadWriteResponse writeBlock(ds.hdfs.Operations.ReadWriteRequest request) {
      return blockingUnaryCall(
          getChannel(), getWriteBlockMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class IDataNodeFutureStub extends io.grpc.stub.AbstractFutureStub<IDataNodeFutureStub> {
    private IDataNodeFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IDataNodeFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IDataNodeFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * For client to read blocks from DataNode
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.ReadWriteResponse> readBlock(
        ds.hdfs.Operations.ReadWriteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getReadBlockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * For client to write blocks to DataNode
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ds.hdfs.Operations.ReadWriteResponse> writeBlock(
        ds.hdfs.Operations.ReadWriteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getWriteBlockMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_READ_BLOCK = 0;
  private static final int METHODID_WRITE_BLOCK = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final IDataNodeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(IDataNodeImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_READ_BLOCK:
          serviceImpl.readBlock((ds.hdfs.Operations.ReadWriteRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse>) responseObserver);
          break;
        case METHODID_WRITE_BLOCK:
          serviceImpl.writeBlock((ds.hdfs.Operations.ReadWriteRequest) request,
              (io.grpc.stub.StreamObserver<ds.hdfs.Operations.ReadWriteResponse>) responseObserver);
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

  private static abstract class IDataNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    IDataNodeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ds.hdfs.proto.HdfsServices.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("IDataNode");
    }
  }

  private static final class IDataNodeFileDescriptorSupplier
      extends IDataNodeBaseDescriptorSupplier {
    IDataNodeFileDescriptorSupplier() {}
  }

  private static final class IDataNodeMethodDescriptorSupplier
      extends IDataNodeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    IDataNodeMethodDescriptorSupplier(String methodName) {
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
      synchronized (IDataNodeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new IDataNodeFileDescriptorSupplier())
              .addMethod(getReadBlockMethod())
              .addMethod(getWriteBlockMethod())
              .build();
        }
      }
    }
    return result;
  }
}
