package net.suqatri.cloud.node;

public class NodeLauncherMain {

    public static void main(String[] args) {
        try {
            new NodeLauncher(args);
        } catch (Exception e) {
            System.out.println("Failed to start node.");
            e.printStackTrace();
        }
    }

}
