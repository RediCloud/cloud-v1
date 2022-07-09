package net.suqatri.cloud.node.node;

import lombok.Getter;
import lombok.Setter;
import net.suqatri.cloud.api.impl.node.CloudNode;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class ClusterConnectionInformation implements Serializable {

    private String name;
    private UUID uniqueId;
    private String hostName;
    private int maxMemory;
    private int maxServiceCount;
    private int maxParallelServiceCount;

    public void applyToNode(CloudNode cloudNode){
        cloudNode.setName(this.name);
        cloudNode.setUniqueId(this.uniqueId);
        cloudNode.setHostname(this.hostName);
        cloudNode.setMaxMemory(this.maxMemory);
        cloudNode.setMaxServiceCount(this.maxServiceCount);
        cloudNode.setMaxParallelStartingServiceCount(this.maxParallelServiceCount);
    }

}
