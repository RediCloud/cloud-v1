package net.suqatri.redicloud.api.redis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
public class RedisCredentials implements Serializable {

    private HashMap<String, Integer> nodeAddresses;
    private String password;
    private int databaseId;
    private RedisType type;
    private boolean ssl = false;

    public String toNodeAddress(String hostname){
        return this.ssl
                ? "rediss://" + hostname + ":" + this.nodeAddresses.get(hostname)
                : "redis://" + hostname + ":" + this.nodeAddresses.get(hostname);
    }

    public String getAnyHostname(){
        return this.toNodeAddress(this.nodeAddresses.keySet().iterator().next());
    }

    public Integer getAnyPort(String hostname){
        return this.nodeAddresses.get(hostname);
    }

    public String getAnyNodeAddress(){
        return this.toNodeAddress(this.getAnyHostname());
    }

}
