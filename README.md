# PoPA Blockchain Simulator

This repository contains the source code for **Proof of Physical Activity (PoPA)** — a novel consensus algorithm that enables mining based on real-world physical work, using data from wearable IoT devices.

---

## 📌 Overview

PoPA modifies the [SimBlock](https://github.com/dsg-titech/simblock) blockchain simulator to:
- Replace traditional mining with **activity-based consensus**
- Reward users proportionally to their **physical effort**
- Integrate **reputation** and **Sybil resistance** mechanisms

---

## 🗂️ Project Structure

```bash
├── simblock/
│   ├── block/
│   │   └── PoPABlock.java         # Custom block class with activity score, reward, hash
│   ├── node/
│   │   └── PoPANode.java          # Node class with activity sensing, reputation, rewards
│   ├── simulator/
│   │   └── MainPoPA.java          # Main class that runs PoPA simulation
│   └── task/
│   │   └── ActivityMiningTask.java     # (optional future extension: PoPA minting tasks)
│
├── settings/
│   └── SimulationConfiguration.java   # Configure number of nodes, intervals, difficulty
├── dist/output/
│   └── output.json          # Logs from the simulation
```

---

## ⚙️ How It Works

- Each node is assigned an **Activity Score (AS)** using simulated sensor input
- A weighted score: `0.7 * AS + 0.3 * Reputation` determines block eligibility
- Eligible nodes mint blocks containing:
  - Activity score
  - Reputation
  - Nonce
  - Proof hash (SHA-256)
- Nodes gain or lose reputation and are rewarded in tokens

---

## 🚀 Running the Simulation

```bash
# Compile
gradle build

# Run simulation
gradle run
```

Output will be logged to `output/output.json`

---

## 🔧 Features

- 🧠 Reputation-based validator scoring
- 🔐 Activity hash proof with device-based uniqueness
- 🎯 Adaptive mining eligibility
- 📈 Simulated fitness mining over PoPA consensus

---

## 🧪 Future Work

- Integration with real IoT input (smartwatch APIs)
- Multi-node committee validation
- Wallets and token economy
- Attack simulations (Sybil, collusion, data spoofing)

---

## 📝 License

This project is licensed under the Apache License 2.0. See `LICENSE` for details.

---

## 👥 Credits

Developed as part of a blockchain thesis by undergraduate students at BRAC University.
Original simulator: [SimBlock](https://github.com/dsg-titech/simblock)
