package net.suqatri.redicloud.commons;

public class ByteUtils {

    public static long bytesToMb(long bytes) {
        return bytes / 1048576;
    }

    public static long mbToBytes(long mb) {
        return mb * 1048576;
    }

}
