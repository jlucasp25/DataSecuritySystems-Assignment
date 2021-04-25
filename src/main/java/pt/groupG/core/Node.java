package pt.groupG.core;

import pt.groupG.communication.NodeDetailsListMessage;
import pt.groupG.communication.NodeDetailsMessage;

import java.util.LinkedList;

public class Node {
    private String nodeID;
    private String address;
    private int port;

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Node(String nodeID, String address, int port) {
        this.nodeID = nodeID;
        this.address = address;
        this.port = port;
    }

    /**
     * Converts a Node into a Protobuf message.
     */
    public static NodeDetailsMessage toMessageFormat(Node node) {
        return NodeDetailsMessage
                .newBuilder()
                .setAddress(node.address)
                .setPort(node.port)
                .setNodeid(node.nodeID)
                .build();
    }

    /**
     * Converts a Node Details Protobuf message into a Node.
     */
    public static Node fromMessageFormat(NodeDetailsMessage msg) {
        return new Node(msg.getNodeid(),msg.getAddress(),msg.getPort());
    }

    /**
     * Converts a Node Details List Protobuf message into a List of Nodes.
     */
    public static LinkedList<Node> fromMessageListFormat(NodeDetailsListMessage msg) {
        LinkedList<Node> l = new LinkedList<Node>();
        for (NodeDetailsMessage ndMsg : msg.getNodesList()) {
            l.add(Node.fromMessageFormat(ndMsg));
        }
        return l;
    }

    /**
     * Converts a list of Nodes into a Node Details List Protobuf message.
     */
    public static NodeDetailsListMessage toMessageListFormat(LinkedList<Node> nodes) {
        LinkedList<NodeDetailsMessage> l = new LinkedList<NodeDetailsMessage>();
        for (Node aux : nodes) {
            l.add(Node.toMessageFormat(aux));
        }
        return NodeDetailsListMessage.newBuilder().addAllNodes(l).build();
    }

    @Override
    public String toString() {
        return "Node" + nodeID + "; " + address + "; " + port;
    }
}
