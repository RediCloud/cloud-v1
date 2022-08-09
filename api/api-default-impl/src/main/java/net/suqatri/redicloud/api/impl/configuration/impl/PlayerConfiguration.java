package net.suqatri.redicloud.api.impl.configuration.impl;

import lombok.Data;
import net.suqatri.redicloud.api.impl.configuration.Configuration;

import java.util.concurrent.TimeUnit;

@Data
public class PlayerConfiguration extends Configuration {

    private String identifier = "players";
    private boolean allowCracked = false;
    private int minPasswordLength = 6;
    private int maxPasswordLength = 18;
    private boolean passwordCanContainsPlayerName = false;

}
