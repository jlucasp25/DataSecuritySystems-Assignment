// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ServerProto.proto

package pt.groupG.grpc;

public final class ServerProtos {
  private ServerProtos() {}
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
      "\n\021ServerProto.proto\022\016pt.groupG.grpc\032\021Cli" +
      "entProto.proto2\277\002\n\rServerService\022A\n\004join" +
      "\022\034.pt.groupG.grpc.EmptyMessage\032\033.pt.grou" +
      "pG.grpc.JoinMessage\022D\n\004ping\022\034.pt.groupG." +
      "grpc.EmptyMessage\032\036.pt.groupG.grpc.Boole" +
      "anMessage\022Q\n\010findNode\022\035.pt.groupG.grpc.N" +
      "odeIdMessage\032&.pt.groupG.grpc.NodeDetail" +
      "sListMessage\022R\n\tfindValue\022\035.pt.groupG.gr" +
      "pc.NodeIdMessage\032&.pt.groupG.grpc.NodeDe" +
      "tailsListMessageB\020B\014ServerProtosP\001b\006prot" +
      "o3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          pt.groupG.grpc.ClientProtos.getDescriptor(),
        });
    pt.groupG.grpc.ClientProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
