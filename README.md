# SimBlock-PoS: Simulating Proof of Stake in SimBlock

This project is based on extending [SimBlock](https://dsg-titech.github.io/simblock/) to support Proof-of-Stake consensus alongside the original Proof-of-Work implementation. The project reworked a [modified version of the PoW Based Simblock](https://github.com/vmardiansyah/modified-simblock) to extend its PoS Functionality.  

## 📄 Paper Summary

**Title:** SimBlock-PoS: Comparative Simulation of Proof-of-Stake and Proof-of-Work  
**Author:** Rodel Advan
**Institution:** BRAC University  
**Abstract:**  
This study extends the SimBlock blockchain simulator to incorporate Proof-of-Stake (PoS) logic. By leveraging an existing PoS block structure and implementing validator selection and stake-based minting logic, the modified simulator enables a direct comparative analysis between PoW and PoS consensus in terms of resource usage and block propagation behavior.

## Features
PoS integrated via Node_PoS, Main_PoS.java

Reuses SampleProofOfStakeBlock.java

Detailed simulation logging for block ID, stake, difficulty, and performance metrics

Compatible with original PoW SimBlock logic


## ⚙️ How to Run
Please ensure you have the Node_PoS file and Main_PoS files selected to make a PoS focused build. 
```bash
git clone https://github.com/rodeladvan1234/SimBlock-PoS.git
cd SimBlock-PoS
./gradlew.bat build
./gradlew.bat run
