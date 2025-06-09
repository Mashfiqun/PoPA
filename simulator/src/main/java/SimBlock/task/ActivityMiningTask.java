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
package SimBlock.task;

import static SimBlock.simulator.Timer.*;

import SimBlock.block.PoPABlock;
import SimBlock.node.Node;
import java.math.BigInteger;

public class ActivityMiningTask extends AbstractMintingTask {

    private double target;

    public ActivityMiningTask(
        Node minter,
        long interval,
        int nonce,
        double target
    ) {
        super(minter, interval, nonce);
        this.target = target;
    }

    @Override
    public void run() {
        // Added and Modified By Viddi
        //		ProofOfWorkBlock createdBlock = new ProofOfWorkBlock((ProofOfWorkBlock)this.getParent(), this.getMinter(), getCurrentTime(), getNoncenya(), this.difficulty);
        PoPABlock createdBlock = new PoPABlock(
            (PoPABlock) this.getParent(),
            this.getMinter(),
            getCurrentTime(),
            ((PoPABlock) getParent()).getActivityScore(),
            ((PoPABlock) getParent()).getActivityHash(),
            getNoncenya()
        ); // Modification By Viddi
        this.getMinter().receiveBlock(createdBlock);
    }
}
