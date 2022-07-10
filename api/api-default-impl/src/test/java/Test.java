import net.suqatri.cloud.api.impl.node.CloudNode;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

public class Test {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://37.114.60.112:6379")
                .setPassword("MV8kF7VEabLvmTwSx")
                .setDatabase(2);
        RedissonClient redissonClient = Redisson.create(config);
        String uuid = "a60409b3-4bd7-4c99-a5f9-a1e6cfac5882";
        RBucket<CloudNode> bucket = redissonClient.getBucket("node@" + uuid, new JsonJacksonCodec());
        System.out.println(bucket.isExists());
        CloudNode cloudNode = bucket.get();
        System.out.println(cloudNode.getUniqueId().toString() + " | " + cloudNode.getName());
    }

}
