import java.net.Socket;
import java.util.Scanner;

public class SocketTest {

    public static void main(String[] args) {
        while (true) {
            try {
                Socket socket = new Socket("localhost", new Scanner(System.in).nextInt());
                System.out.println("Connected to " + socket.getInetAddress());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection failed");
            }
        }

    }

}
