package net.suqatri.cloud.node;

public class NodeLauncherMain {

    public static void main(String[] args) {
        System.out.println("Arguments: " + args.length);
        if(args.length != 0){
            for (String arg : args) {
                System.out.println(" - " + args);
            }
        }
        try {
            new NodeLauncher(args);
        } catch (Exception e) {
            System.out.println("Failed to start node.");
            e.printStackTrace();
        }
    }

}
