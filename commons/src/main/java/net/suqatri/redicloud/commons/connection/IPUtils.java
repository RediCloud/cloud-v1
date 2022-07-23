package net.suqatri.redicloud.commons.connection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class IPUtils {

    private static String cachedIp;

    public static String getPublicIP() {
        if (cachedIp != null) return cachedIp;
        try {
            cachedIp = new BufferedReader(new InputStreamReader(new java.net.URL("https://checkip.amazonaws.com").openConnection().getInputStream())).readLine();
            return cachedIp;
        } catch (Exception e) {
            return "";
        }
    }

}
