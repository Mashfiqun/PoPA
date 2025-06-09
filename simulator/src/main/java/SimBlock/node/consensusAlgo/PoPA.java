/**
 * Copyright 2019 Distributed Systems Group
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
package SimBlock.node.consensusAlgo;

import static SimBlock.simulator.Main.*;

import SimBlock.block.Block;
import SimBlock.block.PoPABlock;
import SimBlock.node.Node;
import SimBlock.node.PoPANode;
import SimBlock.task.ActivityMiningTask;
import java.math.BigInteger;

public class PoPA extends AbstractConsensusAlgo {

    public PoPA(Node selfNode) {
        super(selfNode);
    }

    @Override
    public ActivityMiningTask minting() {
        Node selfNode = this.getSelfNode();
        PoPABlock parent = (PoPABlock) selfNode.getBlock();
        double difficulty = parent.getNextTarget();
        double p = parent.getReward() / difficulty;
        double u = random.nextDouble();
        return p <= Math.pow(2, -53)
            ? null
            : new ActivityMiningTask(
                selfNode,
                (long) ((Math.log(u) / Math.log(1.0 - p)) * 1000),
                0,
                difficulty
            );
    }

    @Override
    public boolean isReceivedBlockValid(
        Block receivedBlock,
        Block currentBlock
    ) {
        if (!(receivedBlock instanceof PoPABlock)) return false;
        PoPABlock _receivedBlock = (PoPABlock) receivedBlock;
        PoPABlock _currentBlock = (PoPABlock) currentBlock;
        int receivedBlockHeight = receivedBlock.getHeight();
        PoPABlock receivedBlockParent = receivedBlockHeight == 0
            ? null
            : (PoPABlock) receivedBlock.getBlockWithHeight(
                receivedBlockHeight - 1
            );

        return (
            (receivedBlockHeight == 0 ||
                _receivedBlock.getTarget() -
                receivedBlockParent.getNextTarget() >=
                0) &&
            (currentBlock == null ||
                _receivedBlock.getTotalWork() - _currentBlock.getTotalWork() >
                0)
        );
    }

    @Override
    public PoPABlock genesisBlock() {
        return PoPABlock.genesisBlock(this.getSelfNode());
    }
}
