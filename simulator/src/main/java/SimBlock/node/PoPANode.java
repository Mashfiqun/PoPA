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

package SimBlock.node;

import static SimBlock.simulator.Main_PoS.OUT_JSON_FILE;
import static SimBlock.simulator.Timer.getCurrentTime;

import SimBlock.block.PoPABlock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

/**
 * PoPA-specific node class that extends the core Node class.
 */
public class PoPANode extends Node {

    private double activityScore;
    private double reputationScore = 1.0;
    private double totalReward = 0.0;
    private String deviceID;
    private String hashType;

    public PoPANode(
        int nodeID,
        int nConnection,
        int region,
        long miningPower,
        String routingTableName,
        String consensusAlgoName
    ) {
        super(
            nodeID,
            nConnection,
            region,
            miningPower,
            routingTableName,
            consensusAlgoName
        );
        assignDeviceID(nodeID);
        assignActivityScore();
    }

    public void assignDeviceID(int nodeId) {
        this.deviceID = "device-" + nodeId + "-" + new Random().nextInt(10000);
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setBlock(PoPABlock block) {
        this.block = block;
    }

    public void printAddBlock(PoPABlock newBlock) {
        hashType = (newBlock.getId() == 0) ? "Genesis Block" : "Chain Block";

        OUT_JSON_FILE.print("{");
        OUT_JSON_FILE.print("\"kind\":\"add-block\",");
        OUT_JSON_FILE.print("\"content\":{");
        OUT_JSON_FILE.print("\"block-Type\":\"" + hashType + "\",");
        OUT_JSON_FILE.print("\"timestamp\":" + getCurrentTime() + ",");
        OUT_JSON_FILE.print("\"node-id\":" + this.getNodeID() + ",");
        OUT_JSON_FILE.print("\"device-id\":" + this.getDeviceID() + ",");
        OUT_JSON_FILE.print(
            "\"activity-score\":" + this.getActivityScore() + ","
        );
        OUT_JSON_FILE.print("\"block-id\":" + newBlock.getId() + ",");
        OUT_JSON_FILE.print(
            "\"prev-Hash\":\"" + newBlock.getPrevHashnya() + "\","
        );
        OUT_JSON_FILE.print("\"nonce\":" + newBlock.getNoncenya() + ",");
        OUT_JSON_FILE.print("\"hash-Value\":\"" + newBlock.getHashnya() + "\"");
        OUT_JSON_FILE.print("}");
        OUT_JSON_FILE.print("},");
        OUT_JSON_FILE.flush();
    }

    public void assignActivityScore() {
        double heartRate = getRandom(70, 160);
        double stepCount = getRandom(2000, 12000);
        double motionIntensity = getRandom(0.1, 1.5);

        this.activityScore =
            normalize(heartRate, 60, 180) * 0.4 +
            normalize(stepCount, 1000, 15000) * 0.4 +
            normalize(motionIntensity, 0.1, 2.0) * 0.2;
    }

    private double getRandom(double min, double max) {
        return min + (max - min) * new Random().nextDouble();
    }

    private double normalize(double val, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
    }

    public double getActivityScore() {
        return this.activityScore;
    }

    public String generateActivityHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data =
                this.deviceID +
                ":" +
                this.activityScore +
                ":" +
                System.currentTimeMillis();
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "error_hash";
        }
    }

    public double getReputationScore() {
        return this.reputationScore;
    }

    public void adjustReputation(boolean successfulBlock) {
        if (successfulBlock) {
            this.reputationScore = Math.min(1.0, this.reputationScore + 0.01);
        } else {
            this.reputationScore = Math.max(0.1, this.reputationScore - 0.01);
        }
    }

    public void accumulateReward(double reward) {
        this.totalReward += reward;
    }

    public double getTotalReward() {
        return this.totalReward;
    }
}
