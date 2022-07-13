import sun.reflect.generics.tree.Tree;

import java.util.*;

public class Test {

    public static void main(String[] args) {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(4, "4");
        map.put(1, "1");
        map.put(3, "3");
        map.put(5, "5");
        map.put(2, "2");

        List<String> list = new ArrayList<>(map.values());

        for (String s : list) {
            System.out.println(s);
        }
    }

}
