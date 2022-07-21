package net.suqatri.cloud.commons.file;

import org.redisson.codec.JsonJacksonCodec;

import java.io.File;
import java.io.IOException;

public class FileWriter {

    public static void write(String filePath, String content) {
        write(new File(filePath), content);
    }

    public static void write(File file, String content) {
        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeObject(Object object, File file) throws Exception{
        write(file, new JsonJacksonCodec().getObjectMapper().writeValueAsString(object));
    }

    public static <T> T readObject(File file, Class<T> clazz) throws Exception{
        return new JsonJacksonCodec().getObjectMapper().readValue(file, clazz);
    }

}
