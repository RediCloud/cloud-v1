import net.suqatri.cloud.api.impl.node.CloudNode;
import net.suqatri.cloud.api.impl.node.TestPacket;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.util.UUID;

public class Test {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://37.114.60.112:6379")
                .setPassword("MV8kF7VEabLvmTwSx")
                .setDatabase(2);
        RedissonClient redissonClient = Redisson.create(config);

        RTopic topic = redissonClient.getTopic("cloud:test", new JsonJacksonCodec());
        TestPacket testPacket = new TestPacket();
        testPacket.setUniqueId(UUID.randomUUID());
        topic.publish(testPacket);
        System.out.println("Published packet: " + testPacket.getUniqueId());
        topic.addListener(TestPacket.class, (charSequence, testPacket1) -> {
            System.out.println("Received Packet: " + testPacket1.getUniqueId());
        });
    }

}
