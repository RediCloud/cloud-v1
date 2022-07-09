package net.suqatri.cloud.commons.file;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeObject(Object object, File file){
        try {
            write(file, new JsonJacksonCodec().getObjectMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static <T> T readObject(File file, Class<T> clazz){
        try {
            return new JsonJacksonCodec().getObjectMapper().readValue(file, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
