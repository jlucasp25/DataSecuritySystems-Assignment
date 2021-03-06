package pt.groupG.core;

import Utils.HashCash;
import com.google.protobuf.ByteString;
import pt.groupG.core.blockchain.Block;
import pt.groupG.core.blockchain.Blockchain;
import pt.groupG.grpc.*;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static Utils.StringUtils.generateRandomString;

/**
 * Main/Core class
 * Contains the starting instructions and manages most of the application.
 */
public class Core {
    // Network Properties
    private final static String SERVER_ADDRESS = "localhost";
    private final static int SERVER_PORT = 8000;
    private static int CLIENT_PORT = 8010;
    /*
    KademliaClientRPC must be called as independently like an HTTP request.
    KademliaBootstrapRPC & KademliaClientChannelRPC must be used as a WebSocket, always open for connections.
    */
    private static KademliaBootstrapChannelRPC serverRPC = null;
    private static KademliaClientChannelRPC clientRPC = null;

    // Kademlia Properties
    public static RoutingTable routingTable = null;
    public static Node selfNode;
    public static ByteString bootstrapId;

    // Blockchain Properties
    public static Blockchain blockchain = new Blockchain();
    public static List<String> trans = new LinkedList<String>();
    public static PowExecutable pow = null;


    public static void main(String[] args) {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Select 1 for regular node, 2 for bootstrap node");
        String s = stdin.next();
        if (s.equals("1")) {
            // send initialWork to bootstrap node for validation
            // only after this validation, can the node join the network
            setupNodeAsRegular();
        }
        else if (s.equals("2")) {
            setupNodeAsBootstrap();
        } else {
            System.out.println("Wrong Option! Exiting.");
            return;
        }
    }

    /**
     * Generates and sets the self node as a Bootstrap one.
     */
    public static void generateBootstrapNode(String address, int port) {
        KademliaKey kKey = new KademliaKey();
        System.out.println("[Bootstrap Key] 0x" + kKey.toHexaString());
        selfNode = new Node(kKey, address, port);
    }

    /**
     * Forces some computation/work to happen before joining the network
     * To prevent Sybil Attack
     */
    public static String sybilAttackPrevention() throws NoSuchAlgorithmException {
        String random = generateRandomString().replace(',','c');
        return HashCash.mintCash(random, 10).toString();
    }

    /**
     * Setups the Node as a regular Peer.
     */
    public static void setupNodeAsRegular() {
        // TEMPORARY: ask for port to attach itself
        CLIENT_PORT = requestClientPort();

        // Generate initial work for POW
        String initialWork = null;
        try {
            initialWork = sybilAttackPrevention();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Regular Node #NONE] Initial Work failed. Exiting!");
            return;
        }

        // sends a JOIN request to the Bootstrap Node
        // to get its own key.
        System.out.println("[Regular Node #NONE] - Sending Join Request...");
        KademliaClientRPC rpc = new KademliaClientRPC(SERVER_ADDRESS, SERVER_PORT);
        JoinMessage joinReq = JoinMessage.newBuilder().setAddress(SERVER_ADDRESS).setPort(CLIENT_PORT).setInitialWork(initialWork).build();
        NodeIdMessage joinRes = rpc.JOIN(joinReq);
        ByteString bootstrapNodeKey = joinRes.getBootstrapnodeidBytes();
        ByteString regularNodeKey = joinRes.getNodeidBytes();
        pt.groupG.grpc.Blockchain bc = joinRes.getBlockchain();
        setBlockchain(bc);

        try {
            rpc.terminateConnection();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bootstrapId = bootstrapNodeKey;
        System.out.println("[My Key] 0x" + new KademliaKey(regularNodeKey).toHexaString());

        // Checks if the initial work is valid.
        if (bootstrapNodeKey.equals("")) {
            System.out.println("Invalid Initial Work found when performing JOIN! Exiting.");
            return;
        }

        // sets its own node ("self") with the ID provided by the JOIN process.
        setSelfNode(regularNodeKey, SERVER_ADDRESS, CLIENT_PORT);

        // generates empty routing table
        routingTable = new RoutingTable(selfNode);

        // start regular node connection channel
        // KademliaClientChannelRPC extends Thread therefore runs in parallel
        clientRPC = new KademliaClientChannelRPC(SERVER_ADDRESS, CLIENT_PORT, routingTable, selfNode, blockchain);
        clientRPC.start();

        // adds the bootstrap node to its routing table.
        routingTable.addNode(new Node(new KademliaKey(bootstrapNodeKey), SERVER_ADDRESS, SERVER_PORT));

        //createBlockPow();

        // send a FIND_NODE request to the bootstrap node
        // so that it finds the closest 3 nodes to itself.
        System.out.println("[Regular Node] - Sending Find Node Request to bootstrap.");
        rpc = new KademliaClientRPC(SERVER_ADDRESS, SERVER_PORT);
        NodeDetailsMessage findNodeReq = NodeDetailsMessage.newBuilder().setNodeidBytes(regularNodeKey).setAddress(SERVER_ADDRESS).setPort(CLIENT_PORT).setBootstrapnodeidBytes(bootstrapNodeKey).build();
        NodeDetailsListMessage findNodeRes = rpc.FIND_NODE(findNodeReq);
        try {
            rpc.terminateConnection();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Lets add the closest nodes to my routing table
        // and send FIND_NODE's to them to populate my table and theirs.
        Set<Contact> totalNearNodes = new HashSet<Contact>();
        Set<Contact> contactedNodes = new HashSet<Contact>();
        contactedNodes.add(new Contact(new KademliaKey(bootstrapId), SERVER_ADDRESS, SERVER_PORT));
        for (NodeDetailsMessage auxMsg : findNodeRes.getNodesList()) {
            totalNearNodes.add(Contact.fromNodeDetailsMessage(auxMsg));
        }

        while (totalNearNodes.size() != 0) {
            // hacky stuff
            Contact nearNode = (Contact) totalNearNodes.toArray()[0];

            System.out.println("[FIND_NODE result (from bootstrap)] 0x" + nearNode.nodeID.toHexaString());
            // add this Node to my routing table.
            routingTable.addNode(Node.fromContact(nearNode));

            // lets add the node to the contacted ones.
            contactedNodes.add(nearNode);
            System.out.println("contacted size: " + contactedNodes.size());
            for (Contact aux : contactedNodes) {

                System.out.println("contacted nodes : " + aux);
            }

            // lets send a find node to the closest node, so the address and port are extracted from the contact. :)
            System.out.println("[Regular Node] - Sending Find Node Request to 0x" + nearNode.nodeID.toHexaString());
            rpc = new KademliaClientRPC(nearNode.getAddress(), nearNode.getPort());
            NodeDetailsMessage cReq = NodeDetailsMessage.newBuilder().setNodeidBytes(ByteString.copyFrom(selfNode.nodeID.byteKey)).setAddress(SERVER_ADDRESS).setPort(CLIENT_PORT).setBootstrapnodeidBytes(bootstrapNodeKey).build();
            NodeDetailsListMessage cRes = rpc.FIND_NODE(cReq);
            try {
                rpc.terminateConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // lets check the new near nodes.
            for (NodeDetailsMessage aux : cRes.getNodesList()) {
                Contact cAux = Contact.fromNodeDetailsMessage(aux);
                System.out.println("[FIND_NODE result (from regular node) 0x" + cAux.nodeID.toHexaString());
                // if this node is new aka hasnt been contacted, add it to the set.
                // if the node hasnt been contacted but its a repeat, it wont be added because its a set.
                if (!contactedNodes.contains(cAux)) {

                    totalNearNodes.add(cAux);
                }
            }
            // lets remove the current node from the list.
            totalNearNodes.remove(nearNode);
        }
        pow = new PowExecutable(selfNode, bootstrapId);
        pow.start();
        Menu();
    }

    /**
     * Transforms blockchain
     */
    public static void setBlockchain(pt.groupG.grpc.Blockchain bc) {
        for (BlockData aux : bc.getBlockList()) {
            List<String> trans = aux.getTList();
            Block block = new Block(trans);
            blockchain.newBlock(block);
        }
    }

    /**
     * Setups the Node as a bootstrap/master Peer.
     */
    public static void setupNodeAsBootstrap() {
        generateBootstrapNode(SERVER_ADDRESS, SERVER_PORT);
        bootstrapId = ByteString.copyFrom(selfNode.nodeID.byteKey);
        routingTable = new RoutingTable(selfNode);
        serverRPC = new KademliaBootstrapChannelRPC(SERVER_ADDRESS, SERVER_PORT, routingTable, selfNode);
        serverRPC.start();
        Menu();
    }

    /**
     * Sets self node using a string key, address and port.
     */
    public static void setSelfNode(ByteString key, String address, int port) {
        KademliaKey kKey = new KademliaKey(key);
        selfNode = new Node(kKey, address, port);
    }

    /**
     * Prompt to request the port for the client node.
     */
    public static int requestClientPort() {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Node Config");
        System.out.println("Enter port for node to attach:");
        return Integer.parseInt(stdin.next());
    }

    public static void Menu() {
        Scanner stdin = new Scanner(System.in);
        System.out.println();
        System.out.println("      MENU SELECTION     ");
        System.out.println("Choose from these choices");
        System.out.println("-------------------------");
        System.out.println("1 - Add money to your wallet");
        System.out.println("2 - Total of your wallet");
        System.out.println("3 - Make a transaction");
        System.out.println("4 - Total of your transactions");
        System.out.println("5 - Quit");
        int choice = Integer.parseInt(stdin.next());

        switch (choice) {
            case 1:
                addMoneyWallet();
                break;
            case 2:
                getMoneyWallet();
                break;
            case 3:
                newTransaction();
                break;
            case 4:
                listTransactions();
                break;
            case 5:
                System.exit(0);
            default:
                System.out.println("Insert an available choice");
        }
    }

    public static void addMoneyWallet() {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Insert your amount: ");
        selfNode.setWallet(Integer.parseInt(stdin.next()));
        Menu();
    }

    public static void getMoneyWallet() {
        System.out.println("Total of your wallet: " + selfNode.getWallet()+"$");
        Menu();
    }

    public static void newTransaction() {
        Scanner stdin = new Scanner(System.in);
        int flag = 1;
        System.out.println("List of your contacts: ");
        List<Contact> allContacts = routingTable.getAllContacts();
        for (Contact aux : allContacts) {
            System.out.println("Contact: " + aux);
        }
        System.out.println("Insert the amount of transaction: ");
        int amount = Integer.parseInt(stdin.next());
        if (amount > selfNode.getWallet())
            System.out.println("You don't have enough money in your wallet to do the transaction");
        else {
            System.out.println("Insert the destination of your transaction: ");
            String dest = stdin.next();
            for (Contact aux : allContacts) {
                if (aux.nodeID.toHexaString().equals(dest)) {
                    trans.add("[New Transaction] " + amount + "$ sent to 0x" + dest);
                    System.out.println("Transaction was made successfully");
                    //increase recipient's wallet
                    KademliaClientRPC rpc = new KademliaClientRPC(aux.getAddress(), aux.getPort());
                    MoneyMessage req = MoneyMessage.newBuilder().setValue(amount).build();
                    EmptyMessage res = rpc.PAY(req);
                    try {
                        rpc.terminateConnection();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //decrease sender's wallet
                    selfNode.setWallet(selfNode.getWallet()-amount);
                    flag = 0;
                }
            }
            if (flag == 1){
                System.out.println("Insert an available recipient.");
            }
        }
    }

    public static void listTransactions() {
        if (trans.size()==0) {
            System.out.println("You have not made any transaction!");
        }
        else {
            System.out.println("List of your transactions: ");
            for (String aux : trans) {
                System.out.println(aux);
            }
        }
        Menu();
    }

}
