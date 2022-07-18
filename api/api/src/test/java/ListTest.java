import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListTest {

    public static void main(String[] args) {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + System.getenv("host") + ":6379")
                .setPassword(System.getenv("password"))
                .setDatabase(2);
        RedissonClient client = Redisson.create(config);
        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook"){
            @Override
            public void run(){
                if(client.isShutdown()) return;
                client.shutdown();
            }
        });

        try {

            RList<TestObject> list = client.getList("test");
            list.clear();
            System.out.println(list.size());
            System.out.println("----------------Added:");
            list.add(new TestObject("test"));
            Thread.sleep(100);
            list.add(new TestObject("test2"));
            Thread.sleep(100);
            list.add(new TestObject("test3"));
            Thread.sleep(100);
            list.add(new TestObject("test4"));
            Thread.sleep(100);
            list.add(new TestObject("test5"));
            Thread.sleep(100);
            list.add(new TestObject("test6"));
            Thread.sleep(100);
            list.add(new TestObject("test7"));
            Thread.sleep(100);
            list.add(new TestObject("test8"));
            Thread.sleep(100);
            list.add(new TestObject("test9"));
            Thread.sleep(100);
            list.add(new TestObject("test10"));

            for (TestObject testObject : list) {
                System.out.println(testObject.getLine());
            }
            System.out.println("---------Sorted:");
            list.sort((o1, o2) -> (int) (o1.getTime() + o2.getTime()));
            for (TestObject testObject : list) {
                System.out.println(testObject.getLine());
            }
            System.out.println("---------Removed:");

            int MAX_LINES = 5;
            for (TestObject testObject : list.readAll()) {
                if(list.size() <= MAX_LINES) break;
                System.out.println("Remove: " + testObject.getLine());
                list.remove(testObject);
            }

            System.out.println("---------Result:");
            for (TestObject testObject : list) {
                System.out.println(testObject.getLine());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
