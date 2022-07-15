package net.suqatri.cloud.commons.file;

import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

@Data
public class FileModifier {

    private final File file;
    private final HashMap<String, Object> replacements = new HashMap<>();

    public void replace() throws Exception{
        String content = "";
        BufferedReader reader = null;
        java.io.FileWriter writer = null;
        reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            content = content + line + System.lineSeparator();
            line = reader.readLine();
        }
        for (String key : replacements.keySet()) {
            key = "%" + key + "%";
            String value = replacements.get(key).toString();
            content = content.replaceAll(key, value);
        }
        writer = new java.io.FileWriter(file);
        writer.write(content);
        reader.close();
        writer.close();
    }

}
