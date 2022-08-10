package dev.redicloud.commons.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.FileWriter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class FileEditor {

    private final List<String> lines;
    private final String splitter;
    private final HashMap<String, String> keyValues;

    public FileEditor(Type type) {
        this.splitter = type.splitter;
        this.lines = new ArrayList<>();
        this.keyValues = new HashMap<>();
    }

    public void read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            fis.close();
            isr.close();
            reader.close();
            this.loadMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(List<String> list) throws IOException {
        this.lines.addAll(list);
        this.loadMap();
    }

    public void save(File file) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        for (String s : newLine()) {
            writer.write(s + "\n");
        }
        writer.close();
    }

    private void loadMap() {
        for (String line : this.lines) {
            if(line.isEmpty()) continue;
            if (getStringWithoutStartSpaces(line).startsWith("-")) continue;
            if (line.endsWith(":")) continue;
            if (getStringWithoutStartSpaces(line).startsWith("#")) continue;
            if(getStringWithoutStartSpaces(line).startsWith("[")) continue;

            String key = "";
            if (!line.contains(this.splitter) || line.split(this.splitter).length < 2) {
                line = getStringWithoutStartSpaces(line);
                key = line.replace(this.splitter, "");
                for (String s : this.splitter.split("")) {
                    key = key.replace(s, "");
                }
                this.keyValues.put(key, "");
                continue;
            }
            try {
                String[] keyValue = line.split(this.splitter);
                if (keyValue[0].startsWith(" ")) {
                    int count = getAmountOfStartSpacesInLine(keyValue[0]);
                    key = keyValue[0].substring(count);
                } else {
                    key = keyValue[0];
                }
                this.keyValues.put(key, keyValue[1]);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Invalid line: " + line);
            }
        }
    }

    public String getStringWithoutStartSpaces(String s){
        int amountOfSpaces = getAmountOfStartSpacesInLine(s);
        return s.substring(amountOfSpaces);
    }

    public void setValue(String key, String value) {
        if (!this.keyValues.containsKey(key)) throw new IllegalArgumentException("Key " + key + " not found");
        this.keyValues.put(key, value);
    }

    public String getValue(String key) {
        return this.keyValues.get(key);
    }

    public List<String> newLine() {
        List<String> list = new ArrayList<>(this.lines);
        this.keyValues.forEach((key, value) -> {
            int lineIndex = getLineIndexByKey(key);
            String newLine = constructNewLine(key, value, lineIndex);
            list.set(lineIndex, newLine);
        });
        return list;
    }

    public String constructNewLine(String key, String value, int lineIndex) {
        String lineWithoutSpaces = key + this.splitter + value;
        int amountOfStartSpaces = getAmountOfStartSpacesInLine(this.lines.get(lineIndex));
        String spacesString = getStringWithSpaces(amountOfStartSpaces);
        return spacesString + lineWithoutSpaces;
    }

    public String getStringWithSpaces(int amount) {
        String spacesString = "";
        for (int i = 0; i < amount; i++) {
            spacesString += " ";
        }
        return spacesString;
    }

    public String removeFirstSpaces(String line) {
        int amountOfSpaces = getAmountOfStartSpacesInLine(line);
        return line.substring(amountOfSpaces);
    }

    public int getAmountOfStartSpacesInLine(String line) {
        int amountOfSpaces = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
                amountOfSpaces++;
                continue;
            }
            break;
        }
        return amountOfSpaces;
    }

    public int getLineIndexByKey(String key) {
        String match = key + this.splitter;
        for (int i = 0; i < this.lines.size(); i++) {
            if(getStringWithoutStartSpaces(this.lines.get(i)).startsWith("#")) continue;
            if (this.lines.get(i).contains(match)) {
                return i;
            }
        }
        return -1;
    }

    @AllArgsConstructor
    @Getter
    public static enum Type {
        YML(": "),
        TOML(" = "),
        PROPERTIES("=");
        private String splitter;
    }

}
