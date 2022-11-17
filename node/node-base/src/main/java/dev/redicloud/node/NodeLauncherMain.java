package dev.redicloud.node;

import dev.redicloud.dependency.DependencyLoader;

import java.text.SimpleDateFormat;

public class NodeLauncherMain {

    public static String[] ARGUMENTS;

    public static void launch(DependencyLoader dependencyLoader, String[] args) {
        ARGUMENTS = args;
        System.out.println("Arguments: " + args.length);
        long sleep = -1L;
        if (args.length != 0) {
            for (String arg : args) {
                if (arg.startsWith("--sleep=")) {
                    sleep = Long.parseLong(arg.substring("--sleep=".length()));
                }
            }
        }
        try {
            if (sleep != -1L) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                System.out.println("Starting node at " + sdf.format(System.currentTimeMillis() + sleep));
                Thread.sleep(sleep);
            }
            new NodeLauncher(dependencyLoader, args);
        } catch (Exception e) {
            System.out.println("Failed to run node:");
            e.printStackTrace();
        }
    }

}
