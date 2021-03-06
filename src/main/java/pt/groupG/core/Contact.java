package pt.groupG.core;

import pt.groupG.grpc.NodeDetailsMessage;

import java.nio.charset.StandardCharsets;

public class Contact extends Node {
    /**
     * Contact
     * Proxy class to represent contact information using Node base class.
     */
    public Contact(KademliaKey nodeID, String address, int port) {
        super(nodeID, address, port);
        try {
            if (address == null)
                throw new Exception("Built Contact without address.");
        }
        catch (Exception e) {
            System.out.println("[BAD NODE ID]: " + nodeID.toHexaString());
        }
    }

    public String toString() {
        return "[Contact] 0x" + nodeID.toHexaString();
    }

    /**
     * Factory Constructor for Contacts using Node.
     */
    public static Contact fromNode(Node nd) {
        return new Contact(nd.nodeID, nd.getAddress(), nd.getPort());
    }

    public static Contact fromNodeDetailsMessage(NodeDetailsMessage msg) {
        KademliaKey kdk = new KademliaKey(msg.getNodeidBytes());
        return new Contact(kdk, msg.getAddress(), msg.getPort());
    }

    @Override
    public boolean equals(Object aux) {
        Contact cAux = (Contact) aux;

        // if both ids are equal, its the same node.
        return this.nodeID.toHexaString().equals(cAux.nodeID.toHexaString());
    }

    // ignore performance improvement of hashset to allow correct equals.
    @Override
    public int hashCode() {
        return 0; // or any other constant
    }

}
