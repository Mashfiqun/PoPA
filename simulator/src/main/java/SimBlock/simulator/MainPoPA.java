/*
 * Copyright 2024 Distributed Systems Group (PoPA Extension)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package SimBlock.simulator;

import static SimBlock.settings.SimulationConfiguration.ALGO;
import static SimBlock.settings.SimulationConfiguration.NUM_OF_NODES;
import static SimBlock.settings.SimulationConfiguration.TABLE;
import static SimBlock.simulator.Network.getDegreeDistribution;
import static SimBlock.simulator.Network.getRegionDistribution;

import SimBlock.block.PoPABlock;
import SimBlock.node.PoPANode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainPoPA {

    public static Random random = new Random(10);

    public static URI CONF_FILE_URI;
    public static URI OUT_FILE_URI;

    static {
        try {
            CONF_FILE_URI = ClassLoader.getSystemResource(
                "simulator.conf"
            ).toURI();
            OUT_FILE_URI = CONF_FILE_URI.resolve(new URI("../output/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static PrintWriter OUT_JSON_FILE;

    static {
        try {
            OUT_JSON_FILE = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(
                        new File(OUT_FILE_URI.resolve("./popa_output.json"))
                    )
                )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("[START] PoPA Simulation...");

        OUT_JSON_FILE.print("[");
        OUT_JSON_FILE.flush();
        double[] regionDistribution = getRegionDistribution();
        List<Integer> regionList = makeRandomList(regionDistribution, false);
        double[] degreeDistribution = getDegreeDistribution();
        List<Integer> degreeList = makeRandomList(degreeDistribution, true);
        int numNodes = NUM_OF_NODES;
        List<PoPANode> nodes = new ArrayList<>();
        for (int i = 1; i <= numNodes; i++) {
            PoPANode node = new PoPANode(
                i,
                degreeList.get(i - 1) + 1,
                regionList.get(i - 1),
                genPower(),
                TABLE,
                ALGO
            );

            // for different activity sources - may be utilized in future
            node.addActivityFromSource(
                "bluetooth_watch",
                0.75 + random.nextDouble() * 0.1
            );
            node.addActivityFromSource(
                "phone_sensor",
                0.65 + random.nextDouble() * 0.1
            );

            nodes.add(node);

            System.out.println(
                "[INIT] Node " +
                node.getNodeID() +
                " | Activity=" +
                String.format("%.3f", node.getActivityScore()) +
                " | Reputation=" +
                String.format("%.3f", node.getReputationScore())
            );
        }

        // Previous Block Gen logic was flawed as parent of all block was set to genesis block
        // Now, each block is generated based on the previous block in the chain

        // Optional tracking maps
        Map<Integer, PoPABlock> blockMap = new HashMap<>(); //Can be used to lookup blocks by ID/hash and parentMap may be used to reconstruct chains / detect forks
        Map<Integer, Integer> parentMap = new HashMap<>();

        List<PoPABlock> allBlocks = new ArrayList<>(); //Simply added for tracking all blocks created during the simulation

        // Create the genesis block
        PoPABlock genesisBlock = PoPABlock.genesisBlock(nodes.get(0));
        allBlocks.add(genesisBlock);
        blockMap.put(genesisBlock.getId(), genesisBlock);
        parentMap.put(genesisBlock.getId(), -1); // Genesis has no parent

        // Set the genesis block as the current tip / parent for the next block
        PoPABlock currentTip = genesisBlock;
        System.out.println(
            "[GENESIS] Block by Node " + nodes.get(0).getNodeID()
        );

        int blockCount = 1;
        double threshold = 0.68;

        // Sort nodes by descending validator weight
        nodes.sort(
            Comparator.comparingDouble(node ->
                -(0.7 * node.getActivityScore() +
                    0.3 * node.getReputationScore())
            )
        );

        //The loop below simulates the mining process
        System.out.println("[START] Mining blocks...");
        for (PoPANode node : nodes) {
            double weighted =
                0.7 * node.getActivityScore() + 0.3 * node.getReputationScore();

            if (weighted >= threshold) {
                String hash = node.generateActivityHash();
                int nonce = random.nextInt(100000);

                PoPABlock newBlock = new PoPABlock(
                    currentTip,
                    node,
                    System.currentTimeMillis(),
                    node.getActivityScore(),
                    hash,
                    nonce
                );

                currentTip = newBlock; // Move tip forward
                allBlocks.add(newBlock);
                blockMap.put(newBlock.getId(), newBlock);
                parentMap.put(newBlock.getId(), newBlock.getParent().getId());

                node.setBlock(newBlock);
                node.printAddBlock(newBlock);
                node.accumulateReward(newBlock.getReward());
                node.incrementBlocksMined();
                node.adjustReputation(true);

                System.out.println(
                    "[BLOCK] Block#" +
                    blockCount++ +
                    " | Node " +
                    node.getNodeID() +
                    " | Reward=" +
                    newBlock.getReward() +
                    " | Rep=" +
                    node.getReputationScore()
                );

                System.out.println(
                    "[DIFFICULTY] New target: " + newBlock.getNextTarget()
                ); //Works after Difficulty adjustment in NodePoPa
            } else {
                node.adjustReputation(false);
                System.out.println(
                    "[FAIL] Node " +
                    node.getNodeID() +
                    " below threshold: " +
                    String.format("%.3f", weighted)
                );
            }
        }

        // PREVIOUS BLOCK GEN LOGIC
        // PoPABlock genesisBlock = PoPABlock.genesisBlock(nodes.get(0));

        // System.out.println(
        //     "[GENESIS] Block by Node " + nodes.get(0).getNodeID()
        // );

        // int blockCount = 1;
        // for (PoPANode node : nodes) {
        //     double weighted =
        //         0.7 * node.getActivityScore() + 0.3 * node.getReputationScore();
        //     double threshold = 0.68;
        //     if (weighted >= threshold) {
        //         String hash = node.generateActivityHash();
        //         int nonce = random.nextInt(100000);
        //         PoPABlock block = new PoPABlock(
        //             genesisBlock,
        //             node,
        //             System.currentTimeMillis(),
        //             node.getActivityScore(),
        //             hash,
        //             nonce
        //         );
        //         node.accumulateReward(block.getReward());
        //         node.adjustReputation(true);
        //         System.out.println(
        //             "[BLOCK] Block#" +
        //             blockCount +
        //             " | Node " +
        //             node.getNodeID() +
        //             " | Reward=" +
        //             block.getReward() +
        //             " | Rep=" +
        //             node.getReputationScore()
        //         );
        //         blockCount++;
        //     } else {
        //         node.adjustReputation(false);
        //         System.out.println(
        //             "[FAIL] Node " +
        //             node.getNodeID() +
        //             " below threshold: " +
        //             String.format("%.3f", weighted)
        //         );
        //     }
        // }

        OUT_JSON_FILE.print("{}]");
        OUT_JSON_FILE.close();

        OUT_JSON_FILE.print("{\"kind\":\"network-summary\",\"nodes\":[");
        for (int i = 0; i < nodes.size(); i++) {
            PoPANode node = nodes.get(i);
            OUT_JSON_FILE.print("{");
            OUT_JSON_FILE.print("\"node-id\":" + node.getNodeID() + ",");
            OUT_JSON_FILE.print(
                "\"device-id\":\"" + node.getDeviceID() + "\","
            );
            OUT_JSON_FILE.print(
                "\"activity-score\":" + node.getActivityScore() + ","
            );
            OUT_JSON_FILE.print(
                "\"reputation-score\":" + node.getReputationScore() + ","
            );
            OUT_JSON_FILE.print(
                "\"total-reward\":" + node.getTotalReward() + ","
            );
            OUT_JSON_FILE.print("\"blocks-mined\":" + node.getBlocksMined());

            OUT_JSON_FILE.print("}");
            if (i != nodes.size() - 1) OUT_JSON_FILE.print(",");
        }
        OUT_JSON_FILE.print("]}");
        OUT_JSON_FILE.print("]");
        OUT_JSON_FILE.close();

        System.out.println(
            "[END] Simulation complete. Blocks mined: " + (blockCount - 1)
        );
    }

    public static ArrayList<Integer> makeRandomList(
        double[] distribution,
        boolean facum
    ) {
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

    public static int genPower() {
        return Math.max((int) (random.nextGaussian() * 10 + 50), 1);
    }
}
