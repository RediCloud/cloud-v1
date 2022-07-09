package net.suqatri.cloud.api.redis;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RedisCredentials implements Serializable {

    private String hostname;
    private int port;
    private String password;
    private int databaseId;

}
