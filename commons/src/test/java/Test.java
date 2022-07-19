import com.google.common.util.concurrent.RateLimiter;
import net.suqatri.cloud.commons.file.FileEditor;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        test();
    }

    private static void test() throws IOException {
        File config = new File("C:\\Users\\pkocz\\Desktop\\Neuer Ordner (3)\\c1\\storage\\config.yml");
        FileEditor editor = new FileEditor(FileEditor.Type.YML);
        editor.read(config);
        Map<String, String> map = editor.getKeyValues();
        editor.setValue("host", "127.0.0.1:25565");
        editor.save(config);
    }

    private static void direct(){
        String splitter = ":";
        String line = "    priorities:";
        if(line.startsWith("#")) return;
        if (!line.contains(splitter) || line.split(splitter).length < 2) {
            System.out.println("1 Key: " + line.replaceAll(splitter, "") + " | Value: " + "");
            return;
        }
        try {
            String[] keyValue = line.split(splitter);
            if(keyValue[0].startsWith(" ")){
                int count = getAmountOfStartSpacesInLine(keyValue[0]);
                String key = keyValue[0].substring(count);
                System.out.println(" 2Key: " + key + " | Value: " + keyValue[1]);
                return;
            }
            System.out.println("3 Key: " + keyValue[0] + " | Value: " + keyValue[1]);
        }catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Invalid line: " + line);
        }
    }

    private static int getAmountOfStartSpacesInLine(String line){
        int amountOfSpaces = 0;
        for(int i = 0; i < line.length(); i++){
            if(line.charAt(i) == ' '){
                amountOfSpaces++;
                continue;
            }
            break;
        }
        return amountOfSpaces;
    }

}
