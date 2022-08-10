package dev.redicloud.commons.file;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    public static byte[] fileToBytes(String filePath) {
        byte[] bytes = null;
        try {
            bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void bytesToFile(byte[] bytes, String filePath) {
        try {
            org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(filePath), bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] mergeByteArrays(Collection<byte[]> list) {
        int length = 0;
        for (byte[] byteArray : list) {
            length += byteArray.length;
        }
        byte[] merged = new byte[length];
        int index = 0;
        for (byte[] byteArray : list) {
            System.arraycopy(byteArray, 0, merged, index, byteArray.length);
            index += byteArray.length;
        }
        return merged;
    }

    public static List<byte[]> splitByteArray(byte[] byteArray, int size) {
        List<byte[]> byteArrays = new ArrayList<>();
        int index = 0;
        while (index < byteArray.length) {
            byteArrays.add(java.util.Arrays.copyOfRange(byteArray, index, index + size));
            index += size;
        }
        return byteArrays;
    }

    public static List<File> getAllFilesAndDirs(File dir) {
        IOFileFilter fileFilter = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }

            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        };
        return org.apache.commons.io.FileUtils.listFilesAndDirs(dir, fileFilter, fileFilter)
                .parallelStream()
                .filter(file -> file != dir)
                .collect(Collectors.toList());
    }

    public static File download(String urlString, File file) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel byteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        byteChannel.close();
        return file;
    }

}
