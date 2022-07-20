![img](https://wakatime.com/badge/github/InkaruNET/cloud.svg)
# RediCloud (WIP⚒️)
A redis based cluster cloud system for minecraft. **[[Discord](https://discord.gg/g2HV52VV4G) | [Developer](https://github.com/Suqatri)]**
<br>

### Overview
  - [What is RediCloud?](#what-is-redicloud)
  - [Features](#features)
  - [Commands](#commands)
  - [Requirements](#requirements)
  - [Installation](#installation)
  <br>

### What is RediCloud?
  - RediCloud is a cluster cloud system for minecraft which is based on [redis](https://redis.io). The best thing is there are no head-nodes and sub-nodes. That means you can easily connect to the cluster. Everything you have to do is to setup the redis connection.
  - Let's imagine you have running your network like the following way:
  
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

### Features 
Please note that this project is still work in progress and not stable yet

(✅ = done | ⚙️ = in progress | ❌ = not started | 🚧 = done, but not tested enough)
  - node clustering ✅
  - redis for communication and storage ✅
  - start minecraft services ([spigot](https://getbukkit.org/download/spigot), [bukkit](https://getbukkit.org/download/craftbukkit), [paper](https://papermc.io) based forks) 🚧
  - start proxy services ([bungeecord](https://www.spigotmc.org/wiki/bungeecord/), [waterfall](https://github.com/PaperMC/Waterfall)) ✅
  - remote screens (with rate limiter for cpu-overload-protection on exception spam) ✅
  - web interface ❌
  - console + commands ✅
  - dynamic and static services 🚧
  - templates (sync via file-nodes) ✅
  - print fatal service errors directly to node console ✅ 
  - api (sync / async) ✅
  - module system ❌
  - default modules: perms, cloudflare ❌
  - redis cluster support ❌
  - smart clustering ❌
  - automatic service start ✅
  - 1.8-1.19 support ✅
  - custom service versions ✅
<br>

## Commands
<br>

### Requirements 
  - Java 8+ Runtime Environment
  - min. 256MB Java Virtual Machine Heap size
  - min. 1GB RAM
  - min. 2 vCores
<br>

### Installation
  - Not done yet
