![img](https://wakatime.com/badge/github/InkaruNET/cloud.svg)
# RediCloud
A redis based cluster cloud system for minecraft. **[[Discord](https://discord.gg/g2HV52VV4G) | [Developer](https://github.com/Suqatri)]**
<br>

### Overview
  - What is RediCloud?
  - Features
  - Minimal requirements
  - Installation
<br>

### What is RediCloud?
  - RediCloud is a cluster cloud system for minecraft which is based on redis db. The best thing is that it's made up of nodes. That means that your servers don't connect to a wrapper or something.
Let's imagine you have running your network like the following way:
  
        Node-1
          ├ Proxy
          ├ Hub
          ├ Practice
          └ FFA
          
        Node-2
          ├ BedWars
          └ SkyWars

  - If `Node-2` is going down for any reason, what would be with BedWars and SkyWars? This is where our node principle comes into play, because if one node fails, the other node(s) take over the work of the failing node. So in our case the network structure would be the following way:

        Node-1
          ├ Proxy
          ├ Hub
          ├ Practice
          ├ FFA
          ├ BedWars (took over from from Node-2)
          └ SkyWars (took over from from Node-2)
          
        Node-2 (failed)    
<br>

### Features (❌ = not done yet, ✅ = done, 🚧 = done, no functional guarantee)
  - Node-System ✅
  - More will be following
<br>

### Minimal requirements
  - Java 8 Runtime Environment
  - 256MB Java Virtual Machine Heap size
  - 1GB RAM
  - 2 vCores
<br>

### Installation
  - Not done yet
