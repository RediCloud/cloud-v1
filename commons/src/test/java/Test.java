import net.suqatri.cloud.commons.file.FileEditor;
import sun.reflect.generics.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Test {

    public static void main(String[] args) throws IOException {
        FileEditor fileEditor = new FileEditor(FileEditor.Type.PROPERTIES);
        fileEditor.read(new File("C:\\Users\\pkocz\\Desktop\\Neuer Ordner (3)\\c1\\storage\\server.properties"));

        fileEditor.setValue("generator-settings", "-11");
        fileEditor.setValue("gamemode", "1");
    }

}
