package net.suqatri.cloud.commons;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static boolean isOpen(InputStream stream) {
        if(stream == null) return false;
        try {
           stream.available();
           return true;
        } catch (IOException e) {
            return false;
        }
    }

}
