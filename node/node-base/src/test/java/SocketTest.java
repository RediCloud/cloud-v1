import net.suqatri.cloud.node.scheduler.Scheduler;

import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SocketTest {

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        scheduler.scheduleTaskAsync(() -> {
            System.out.println("Hello World");
        }, 1, 1, TimeUnit.SECONDS);
    }

}
