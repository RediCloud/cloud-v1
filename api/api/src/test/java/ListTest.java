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

    public static void main(String[] args) {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + System.getenv("host") + ":6379")
                .setPassword(System.getenv("password"))
                .setDatabase(2);
        RedissonClient client = Redisson.create(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(client.isShutdown()) return;
            client.shutdown();
        }));

        RMap<String, UUID> map = client.getMap("map", new JsonJacksonCodec());

        map.putAsync("test", UUID.randomUUID());
        System.out.println(map.get("test").toString());

    }

}
