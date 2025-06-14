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

package SimBlock.block;

import SimBlock.node.Node;

/**
 * The type Proof of Physical Activity block.
 */
public class PoPABlock extends Block {

    private final double activityScore;
    private final String activityHash;
    private final double totalWork;
    private final double nextTarget;
    private final double reward;
    private static double genesisNextTarget;
    private static double baseRewardRate = 10.0; // Tokens per full activity point

    /**
     * Instantiates a new PoPA block.
     *
     * @param parent the parent block
     * @param minter the node that mined the block
     * @param time the time the block was mined
     * @param activityScore the physical activity score
     * @param activityHash the hashed proof of physical work
     */
    public PoPABlock(
        // PoPABlock parent, //Tracks the first parent - GENESIS ONLY : FIXED
        // int Noncenya
        Block parent, //Changed to Block for compatibility
        Node minter,
        long time,
        double activityScore,
        String activityHash,
        int nonce
    ) {
        super(parent, minter, time, nonce);
        this.activityScore = activityScore;
        this.activityHash = activityHash;
        this.reward = calculateReward(activityScore);

        if (parent == null) {
            this.totalWork = activityScore;
            this.nextTarget = PoPABlock.genesisNextTarget;
        } else if (parent instanceof PoPABlock p) {
            this.totalWork = p.getTotalWork() + activityScore;
            this.nextTarget = adjustDifficulty(p);
        } else {
            this.totalWork = activityScore;
            this.nextTarget = PoPABlock.genesisNextTarget;
        }
    }

    //---Difficulty adjustment based on total work---//
    private double adjustDifficulty(PoPABlock parent) {
    double newTarget = parent.getNextTarget();
    double delta = this.activityScore - parent.getActivityScore();

    if (delta > 0.1) {
        newTarget *= 0.98; // Increase difficulty slightly
    } else if (delta < -0.1) {
        newTarget *= 1.02; // Decrease difficulty
    }
    return Math.max(0.1, Math.min(1.0, newTarget));
}


    /**
     * Gets activity score.
     *
     * @return the activity score
     */
    public double getActivityScore() {
        return this.activityScore;
    }

    /**
     * Gets activity hash.
     *
     * @return the activity hash
     */
    public String getActivityHash() {
        return this.activityHash;
    }

    /**
     * Gets cumulative work based on activity.
     *
     * @return the total work
     */
    public double getTotalWork() {
        return this.totalWork;
    }

    /**
     * Gets next target threshold for mining.
     *
     * @return the next target
     */
    public double getNextTarget() {
        return this.nextTarget;
    }

    public double getTarget() {
        return this.nextTarget;
    }

    /**
     * Gets the reward for mining this block.
     *
     * @return the reward
     */
    public double getReward() {
        return this.reward;
    }

    /**
     * Reward calculation function based on activity score.
     * Can be adjusted for fairness and inflation control.
     *
     * @param activityScore the activity score used in mining
     * @return the reward amount
     */
    private double calculateReward(double activityScore) {
        return activityScore * baseRewardRate;
    }

    /**
     * Generates the genesis block for PoPA.
     *
     * @param minter the initial minter node
     * @return the genesis PoPABlock
     */
    public static PoPABlock genesisBlock(Node minter) {
        double baselineActivity = 0.75;
        PoPABlock.genesisNextTarget = 0.70; // initial difficulty
        return new PoPABlock(
            null,
            minter,
            0,
            baselineActivity,
            "genesis_hash",
            0
        );
    }
}
