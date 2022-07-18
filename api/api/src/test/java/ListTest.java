import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.ListAddListener;
import org.redisson.api.listener.SetObjectListener;
import org.redisson.codec.JsonJacksonCodec;
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

            RList<String> list = client.getList("test-string", new JsonJacksonCodec());
            list.clear();

            list.addListenerAsync(new ListAddListener() {
                @Override
                public void onListAdd(String s) {
                    System.out.println("Called");
                    System.out.println("add: " + s);
                }
            });

            list.add("a");
            list.add("b");
            list.add("c");
            System.out.println(list.size());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
