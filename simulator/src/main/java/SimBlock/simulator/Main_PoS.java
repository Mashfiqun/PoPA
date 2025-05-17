package SimBlock.simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import SimBlock.block.SampleProofOfStakeBlock;
import SimBlock.node.Node;
import SimBlock.node.Node_PoS;
import static SimBlock.settings.SimulationConfiguration.ALGO;
import static SimBlock.settings.SimulationConfiguration.ENDBLOCKHEIGHT;
import static SimBlock.settings.SimulationConfiguration.INTERVAL;
import static SimBlock.settings.SimulationConfiguration.NUM_OF_NODES;
import static SimBlock.settings.SimulationConfiguration.TABLE;
import static SimBlock.simulator.Network.getDegreeDistribution;
import static SimBlock.simulator.Network.getRegionDistribution;
import static SimBlock.simulator.Network.printRegion;
import static SimBlock.simulator.Simulator.addNode;
import static SimBlock.simulator.Simulator.getSimulatedNodes;
import static SimBlock.simulator.Simulator.setTargetInterval;
import static SimBlock.simulator.Timer.getCurrentTime;

public class Main_PoS {
  public static Random random = new Random(10);
  public static long time1 = 0;

  public static URI CONF_FILE_URI;
  public static URI OUT_FILE_URI;
  static {
    try {
      CONF_FILE_URI = ClassLoader.getSystemResource("simulator.conf").toURI();
      OUT_FILE_URI = CONF_FILE_URI.resolve(new URI("../output/"));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public static PrintWriter OUT_JSON_FILE;
  public static PrintWriter STATIC_JSON_FILE;
  static {
    try {
      OUT_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./output.json")))));
      STATIC_JSON_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./static.json")))));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    java.time.LocalTime compileStart = java.time.LocalTime.now();
    System.out.println("Start PoS Simulation");

    long start = System.currentTimeMillis();
    setTargetInterval(INTERVAL);

    OUT_JSON_FILE.print("[");
    OUT_JSON_FILE.flush();

    printRegion();

    constructPoSNetwork(NUM_OF_NODES);

    int height = 1;
    SampleProofOfStakeBlock newBlock = null;
    Node_PoS validator = null;

    while (height <= ENDBLOCKHEIGHT) {
      validator = selectValidatorByStake(getSimulatedNodes());
      SampleProofOfStakeBlock parent = (SampleProofOfStakeBlock) validator.getBlock();

      if (parent == null) {
        continue;
      }

      newBlock = new SampleProofOfStakeBlock(
        parent,
        validator,
        getCurrentTime(),
        random.nextInt(100000),
        parent.getNextDifficulty()
      );

      validator.setBlock(newBlock);
      validator.printAddBlock(newBlock);
      validator.sendInv(newBlock);

      height++;
    }

    long end = System.currentTimeMillis();
    time1 += end - start;
    java.time.LocalTime compileEnd = java.time.LocalTime.now();

    System.out.println("\n===== Final Block Summary =====");
    System.out.println("Block Height       : " + newBlock.getHeight());
    System.out.println("Validator Node ID  : " + validator.getNodeID());
    System.out.println("Stake              : " + validator.getStake());
    System.out.println("Timestamp          : " + newBlock.getTime());
    System.out.println("Block ID           : " + newBlock.getId());
    System.out.println("Parent Block ID    : " + (newBlock.getParent() != null ? newBlock.getParent().getId() : "None"));
    System.out.println("Difficulty         : " + newBlock.getDifficulty());
    System.out.println("-------------------------------");
    System.out.println("Start time         : " + start);
    System.out.println("End time           : " + end);
    System.out.println("Duration time      : " + time1 + " ms");
    System.out.println("Duration time      : " + (time1 / 1000) + " seconds");
    System.out.println("Number of NODE     : " + NUM_OF_NODES);
    System.out.println("Number of BLOCK    : " + ENDBLOCKHEIGHT);
    System.out.println("Compile Time Start : " + compileStart);
    System.out.println("Compile Time Stop  : " + compileEnd);

    OUT_JSON_FILE.print("{");
    OUT_JSON_FILE.print("\"kind\":\"simulation-end\",");
    OUT_JSON_FILE.print("\"content\":{");
    OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime());
    OUT_JSON_FILE.print("}");
    OUT_JSON_FILE.print("]");
    OUT_JSON_FILE.close();

    System.out.println("Simulation Duration: " + time1 + " ms");
  }

  public static Node_PoS selectValidatorByStake(List<Node> nodeList) {
    List<Node_PoS> stakeNodes = new ArrayList<>();
    for (Node n : nodeList) {
      stakeNodes.add((Node_PoS) n);
    }

    int totalStake = stakeNodes.stream().mapToInt(Node_PoS::getStake).sum();
    int rand = new Random().nextInt(totalStake);

    int cumulative = 0;
    for (Node_PoS node : stakeNodes) {
      cumulative += node.getStake();
      if (rand < cumulative) {
        return node;
      }
    }
    return stakeNodes.get(0);
  }

  public static int genStake() {
    return Math.max((int)(random.nextGaussian() * 10 + 50), 1);
  }

  public static void constructPoSNetwork(int numNodes) {
    double[] regionDistribution = getRegionDistribution();
    List<Integer> regionList = makeRandomList(regionDistribution, false);
    double[] degreeDistribution = getDegreeDistribution();
    List<Integer> degreeList = makeRandomList(degreeDistribution, true);

    for (int id = 1; id <= numNodes; id++) {
      Node_PoS node = new Node_PoS(id, degreeList.get(id - 1) + 1, regionList.get(id - 1), genStake(), TABLE, ALGO);
      addNode(node);

      OUT_JSON_FILE.print("{");
      OUT_JSON_FILE.print("\"kind\":\"add-node\",");
      OUT_JSON_FILE.print("\"content\":{");
      OUT_JSON_FILE.print("\"timestamp\":0,");
      OUT_JSON_FILE.print("\"node-id\":" + id + ",");
      OUT_JSON_FILE.print("\"region-id\":" + regionList.get(id - 1));
      OUT_JSON_FILE.print("}");
      OUT_JSON_FILE.print("},");
      OUT_JSON_FILE.flush();
    }

    for (Node node : getSimulatedNodes()) {
      node.joinNetwork();
    }

    Node_PoS genesisMinter = (Node_PoS) getSimulatedNodes().get(0);
    SampleProofOfStakeBlock genesis = SampleProofOfStakeBlock.genesisBlock(genesisMinter);
    genesisMinter.setBlock(genesis);
    genesisMinter.printAddBlock(genesis);
    genesisMinter.sendInv(genesis);
  }

  public static ArrayList<Integer> makeRandomList(double[] distribution, boolean facum) {
    ArrayList<Integer> list = new ArrayList<>();
    int index = 0;
    if (facum) {
      for (; index < distribution.length; index++) {
        while (list.size() <= NUM_OF_NODES * distribution[index]) {
          list.add(index);
        }
      }
    } else {
      double acc = 0.0;
      for (; index < distribution.length; index++) {
        acc += distribution[index];
        while (list.size() <= NUM_OF_NODES * acc) {
          list.add(index);
        }
      }
    }
    while (list.size() < NUM_OF_NODES) {
      list.add(index);
    }
    Collections.shuffle(list, random);
    return list;
  }
}
