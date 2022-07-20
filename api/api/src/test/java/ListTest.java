import net.suqatri.cloud.api.CloudAPI;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.ListAddListener;
import org.redisson.api.listener.SetObjectListener;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ListTest {

    public static void main(String[] args) throws InterruptedException {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + System.getenv("host") + ":6379")
                .setPassword(System.getenv("password"))
                .setDatabase(1);
        RedissonClient client = Redisson.create(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(client.isShutdown()) return;
            client.shutdown();
        }));

        RMap<String, String> map = client.getMap("service@idFetcher", new JsonJacksonCodec());
        map.readAllKeySetAsync().whenComplete((s, s1) -> {
            if(s1 != null) {
                System.out.println("Error: " + s1.getMessage());
                return;
            }
            for (String s2 : s) {
                System.out.println(s2);
            }
        });
    }

}
