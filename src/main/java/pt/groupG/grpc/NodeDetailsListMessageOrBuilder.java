// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/proto/ClientProto.proto

package pt.groupG.grpc;

public interface NodeDetailsListMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:pt.groupG.grpc.NodeDetailsListMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .pt.groupG.grpc.NodeDetailsMessage nodes = 1;</code>
   */
  java.util.List<pt.groupG.grpc.NodeDetailsMessage> 
      getNodesList();
  /**
   * <code>repeated .pt.groupG.grpc.NodeDetailsMessage nodes = 1;</code>
   */
  pt.groupG.grpc.NodeDetailsMessage getNodes(int index);
  /**
   * <code>repeated .pt.groupG.grpc.NodeDetailsMessage nodes = 1;</code>
   */
  int getNodesCount();
  /**
   * <code>repeated .pt.groupG.grpc.NodeDetailsMessage nodes = 1;</code>
   */
  java.util.List<? extends pt.groupG.grpc.NodeDetailsMessageOrBuilder> 
      getNodesOrBuilderList();
  /**
   * <code>repeated .pt.groupG.grpc.NodeDetailsMessage nodes = 1;</code>
   */
  pt.groupG.grpc.NodeDetailsMessageOrBuilder getNodesOrBuilder(
      int index);
}