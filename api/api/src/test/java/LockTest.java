import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LockTest {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://37.114.60.112:6379")
                .setPassword("MV8kF7VEabLvmTwSx")
                .setDatabase(2);
        RedissonClient client = Redisson.create(config);

        RLock lock = client.getFairLock("test:fair-lock");

        Thread thread1 = new Thread(() -> {
            lock.tryLockAsync(10, -1, TimeUnit.SECONDS).whenComplete((v, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println("1: " + v);
                    System.out.println("1: Lock acquired");
                    lock.lockAsync();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    lock.unlockAsync();
                    System.out.println("1: Lock released");
                }
            });
        });

        Thread thread2 = new Thread(() -> {
            lock.tryLockAsync(10, -1, TimeUnit.SECONDS).whenComplete((v, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println("2: " + v);
                    System.out.println("2: Lock acquired");
                    lock.lockAsync();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    lock.unlockAsync();
                    System.out.println("2: Lock released");
                }
            });
        });

        Thread thread3 = new Thread(() -> {
            while(!client.isShutdown()){
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Lock-State: " + lock.isLocked());
            }
        });

        thread3.start();
        thread2.start();
        thread1.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("SHutdown");
                client.shutdown();
                System.out.println(1);
            }
        }, 20*1000);
    }

}
