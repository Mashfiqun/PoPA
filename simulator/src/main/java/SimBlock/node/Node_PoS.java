// UPDATED Node_PoS.java for PoS Integration

package SimBlock.node;

// import java.util.ArrayList;
// import java.util.HashSet;
import java.util.Random;
// import java.util.Set;

import SimBlock.block.Block;
import SimBlock.block.SampleProofOfStakeBlock;
import static SimBlock.simulator.Main_PoS.OUT_JSON_FILE;
import static SimBlock.simulator.Timer.getCurrentTime;
// import SimBlock.task.AbstractMintingTask;
// import SimBlock.task.RecMessageTask;

public class Node_PoS extends Node {
    private int stake;

    // private Block block;
    // private Set<Block> orphans = new HashSet<>();
    // private AbstractMintingTask mintingTask = null;
    // private boolean sendingBlock = false;
    // private ArrayList<RecMessageTask> messageQue = new ArrayList<>();
    // private Set<Block> downloadingBlocks = new HashSet<>();

    // private long processingTime = 2;
    private String hashType;

	public Node_PoS(int nodeID, int nConnection, int region, long miningPower, String routingTableName, String consensusAlgoName) {
		super(nodeID, nConnection, region, miningPower, routingTableName, consensusAlgoName);
		this.stake = new Random().nextInt(100) + 1;
	}
	

    // public int getNodeID() { return this.nodeID; }
    // public int getRegion() { return this.region; }
    // public long getMiningPower() { return this.miningPower; }
    public int getStake() { return this.stake; }
    // public AbstractConsensusAlgo getConsensusAlgo() { return this.consensusAlgo; }
    // public AbstractRoutingTable getRoutingTable() { return this.routingTable; }
    // public Block getBlock() { return this.block; }
    public void setBlock(Block block) { this.block = block; }
    // public Set<Block> getOrphans() { return this.orphans; }

    // public int getnConnection() { return this.routingTable.getnConnection(); }
    // public void setnConnection(int nConnection) { this.routingTable.setnConnection(nConnection); }
    // public ArrayList<Node> getNeighbors() { return this.routingTable.getNeighbors(); }
    // public boolean addNeighbor(Node node) { return this.routingTable.addNeighbor(node); }
    // public boolean removeNeighbor(Node node) { return this.routingTable.removeNeighbor(node); }

    // public void joinNetwork() { this.routingTable.initTable(); }

    // public void addToChain(Block newBlock) {
    //     if (this.mintingTask != null) {
    //         removeTask(this.mintingTask);
    //         this.mintingTask = null;
    //     }
    //     this.block = newBlock;
    //     printAddBlock(newBlock);
    //     arriveBlock(newBlock, this);
    // }

    public void printAddBlock(Block newBlock) {
        hashType = (newBlock.getId() == 0) ? "Genesis Block" : "Chain Block";

        OUT_JSON_FILE.print("{");
        OUT_JSON_FILE.print("\"kind\":\"add-block\",");
        OUT_JSON_FILE.print("\"content\":{");
        OUT_JSON_FILE.print("\"block-Type\":\"" + hashType + "\",");
        OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
        OUT_JSON_FILE.print("\"node-id\":" + this.getNodeID() + ",");
        OUT_JSON_FILE.print("\"block-id\":" + newBlock.getId() + ",");
        OUT_JSON_FILE.print("\"prev-Hash\":\"" + newBlock.getPrevHashnya() + "\",");
        OUT_JSON_FILE.print("\"nonce\":" + newBlock.getNoncenya() + ",");
        OUT_JSON_FILE.print("\"hash-Value\":\"" + newBlock.getHashnya() + "\"");
        OUT_JSON_FILE.print("}");
        OUT_JSON_FILE.print("},");
        OUT_JSON_FILE.flush();
    }

    public void createPoSBlock(SampleProofOfStakeBlock parent) {
        SampleProofOfStakeBlock newBlock = new SampleProofOfStakeBlock(
            parent,
            this,
            getCurrentTime(),
            new Random().nextInt(100000),
            parent.getNextDifficulty()
        );
        setBlock(newBlock);
        printAddBlock(newBlock);
        sendInv(newBlock);
    }

    // public void receiveBlock(Block block) {
    //     if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
    //         if (this.block != null && !this.block.isOnSameChainAs(block)) {
    //             this.addOrphans(this.block, block);
    //         }
    //         this.addToChain(block);
    //         this.sendInv(block);
    //     } else if (!this.orphans.contains(block) && !block.isOnSameChainAs(this.block)) {
    //         this.addOrphans(block, this.block);
    //         arriveBlock(block, this);
    //     }
    // }

    // public void sendInv(Block block) {
    //     for (Node to : this.routingTable.getNeighbors()) {
    //         AbstractMessageTask task = new InvMessageTask(this, to, block);
    //         putTask(task);
    //     }
    // }

    // public void receiveMessage(AbstractMessageTask message) {
    //     Node from = message.getFrom();

    //     if (message instanceof InvMessageTask) {
    //         Block block = ((InvMessageTask) message).getBlock();
    //         if (!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)) {
    //             if (this.consensusAlgo.isReceivedBlockValid(block, this.block)) {
    //                 AbstractMessageTask task = new RecMessageTask(this, from, block);
    //                 putTask(task);
    //                 downloadingBlocks.add(block);
    //             } else if (!block.isOnSameChainAs(this.block)) {
    //                 AbstractMessageTask task = new RecMessageTask(this, from, block);
    //                 putTask(task);
    //                 downloadingBlocks.add(block);
    //             }
    //         }
    //     }

    //     if (message instanceof RecMessageTask) {
    //         this.messageQue.add((RecMessageTask) message);
    //         if (!sendingBlock) {
    //             this.sendNextBlockMessage();
    //         }
    //     }

    //     if (message instanceof BlockMessageTask) {
    //         Block block = ((BlockMessageTask) message).getBlock();
    //         downloadingBlocks.remove(block);
    //         this.receiveBlock(block);
    //     }
    // }

    // public void sendNextBlockMessage() {
    //     if (!this.messageQue.isEmpty()) {
    //         sendingBlock = true;

    //         Node to = this.messageQue.get(0).getFrom();
    //         Block block = this.messageQue.get(0).getBlock();
    //         this.messageQue.remove(0);
    //         long blockSize = BLOCKSIZE;
    //         long bandwidth = getBandwidth(this.getRegion(), to.getRegion());
    //         long delay = blockSize * 8 / (bandwidth / 1000) + processingTime;
    //         BlockMessageTask messageTask = new BlockMessageTask(this, to, block, delay);

    //         putTask(messageTask);
    //     } else {
    //         sendingBlock = false;
    //     }
    // }

    // public void addOrphans(Block orphanBlock, Block validBlock) {
    //     if (orphanBlock != validBlock) {
    //         this.orphans.add(orphanBlock);
    //         this.orphans.remove(validBlock);
    //         if (validBlock == null || orphanBlock.getHeight() > validBlock.getHeight()) {
    //             this.addOrphans(orphanBlock.getParent(), validBlock);
    //         } else if (orphanBlock.getHeight() == validBlock.getHeight()) {
    //             this.addOrphans(orphanBlock.getParent(), validBlock.getParent());
    //         } else {
    //             this.addOrphans(orphanBlock, validBlock.getParent());
    //         }
    //     }
    // }
}
