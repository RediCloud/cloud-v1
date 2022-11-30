![img](https://img.shields.io/nexus/r/dev.redicloud.api/api?label=release&server=https%3A%2F%2Frepo.redicloud.dev)
![img](https://img.shields.io/nexus/s/dev.redicloud.api/api?label=snapshot&server=https%3A%2F%2Frepo.redicloud.dev)

# RediCloud

A redis based cluster cloud system for
minecraft. **[[Discord](https://discord.gg/g2HV52VV4G) | [Developer](https://github.com/Suqatri)]**
<br>

### Overview

- [What is RediCloud?](#what-is-redicloud)
- [Features](#features)
- [Commands](#commands)
- [Requirements](#requirements)
- [Installation](#installation)
<br>

### What is RediCloud?

- RediCloud is a cluster cloud system for minecraft which is based on [redis](https://redis.io). The best thing is there
  are no head-nodes and sub-nodes. That means you can easily connect to the cluster. Everything you have to do is to
  setup the redis connection.
- Let's imagine you have running your network like the following way:

      Node-1
        ├ Proxy
        ├ Hub
        ├ Practice
        └ FFA
        
      Node-2
        ├ BedWars
        └ SkyWars

- If `Node-2` is going down for any reason, what would be with BedWars and SkyWars? This is where our node principle
  comes into play, because if one node fails, the other node(s) take over the work of the failing node. So in our case
  the network structure would be the following way:

      Node-1
        ├ Proxy
        ├ Hub
        ├ Practice
        ├ FFA
        ├ BedWars (took over from Node-2)
        └ SkyWars (took over from Node-2)
        
      Node-2 (failed)    

<br>

### Features

(✅ = done | ⚙️ = in progress | ❌ = not started | 🚧 = done, but not tested enough)

- node clustering ✅
- redis for communication and storage ✅
- start minecraft services ([spigot](https://getbukkit.org/download/spigot)
  , [bukkit](https://getbukkit.org/download/craftbukkit), [paper](https://papermc.io) based forks) ✅
- start proxy services ([bungeecord](https://www.spigotmc.org/wiki/bungeecord/)
  , [waterfall](https://github.com/PaperMC/Waterfall), [velocity](https://github.com/PaperMC/Velocity)) ✅
- remote screens (with rate limiter for cpu-overload-protection on exception spam) ✅
- web interface ❌
- console + commands ✅
- dynamic and static services ✅
- templates (sync via file-nodes) ✅
- print fatal service errors directly to node console ✅
- api (sync / async) ✅
- module system ⚙️
- default modules: perms, cloudflare ❌
- redis cluster support🚧
- smart clustering ⚙️
- automatic service start ✅
- 1.8-1.19 support ✅
- custom service versions ✅
- java start command is customizable for each service version ✅
- modify programm arguments and jvm flags for each group ✅
- external proxy services (start external proxy services and connect cloud services to them) ✅
- easy dev plugin test (create external service, that you can start for e.g via your IDE. The services will connect without a node to the cloud cluster) 🚧
- offline/online player support at the same time ✅
- player api bridge (actionbar, messages, service connect, kick, title, tablist) ✅
- toggle maintenance for groups/services✅
- multi proxy (with player count sync)✅
- limbo fallbacks✅
- only proxy join (but please use your firewall: [guide](https://www.spigotmc.org/wiki/firewall-guide/))🚧
  <br>

## Commands


<details>
  <summary>General help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/generel_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/generel_help.png">
  </picture>
</details>


<details>
  <summary>Cluster help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/cluster_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/cluster_help.png">
  </picture>
</details>

<details>
  <summary>Template help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/template_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/template_help.png">
  </picture>
</details>

<details>
  <summary>Group help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/group_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/group_help.png">
  </picture>
</details>

<details>
  <summary>Service verison help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/service_version_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/service_version_help.png">
  </picture>
</details>

<details>
  <summary>Service help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/service_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/service_help.png">
  </picture>
</details>


<details>
  <summary>Screen help</summary>
  <picture>
    <source srcset="https://content.redicloud.dev/github-img/screen_help.png" media="(min-width: 600px)">
    <img src="https://content.redicloud.dev/github-img/screen_help.png">
  </picture>
</details>

<br>

### Requirements

- Java 8+ Runtime Environment
- min. 256MB Java Virtual Machine Heap size
- min. 1GB RAM
- min. 2 vCores
- a redis server
  <br>

### Installation

Follow these [steps](https://github.com/RediCloud/cloud/wiki/installation)
<br>

### Build

Last Build: [jenkins](http://ci.redicloud.dev/job/redi-cloud/)

Linux / OSX
```
git clone https://github.com/RediCloud/cloud
cd cloud
./gradlew build
```

Windows
```
git clone https://github.com/RediCloud/cloud
cd cloud
gradlew.bat
```
<br>

### API

Repository
```
maven {
    url = "https://repo.redicloud.dev/repository/maven-snapshots/"
}

maven {
    url = "https://repo.redicloud.dev/repository/maven-releases/"
}
```

Dependencies
```
implementation('dev.redicloud.api:api:VERSION')
```
