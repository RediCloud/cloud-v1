package dev.redicloud.updater;

import com.google.common.util.concurrent.AtomicDouble;
import dev.redicloud.commons.MathUtils;
import dev.redicloud.commons.OSValidator;
import dev.redicloud.commons.file.ZipUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoUpdater {

    private static String[] arguments;
    private static AtomicDouble progress = new AtomicDouble(0);

    public static void main(String[] args) throws Exception{
        log("");
        log("     _______                 __   _      ______  __                         __  ");
        log("    |_   __ \\               |  ] (_)   .' ___  |[  |                       |  ] ");
        log("      | |__) |  .---.   .--.| |  __   / .'   \\_| | |  .--.   __   _    .--.| |  ");
        log("      |  __ /  / /__\\\\/ /'`\\' | [  |  | |        | |/ .'`\\ \\[  | | | / /'`\\' |  ");
        log("     _| |  \\ \\_| \\__.,| \\__/  |  | |  \\ `.___.'\\ | || \\__. | | \\_/ |,| \\__/  |  ");
        log("    |____| |___|'.__.' '.__.;__][___]  `.____ .'[___]'.__.'  '.__.'_/ '.__.;__] ");
        log("");
        log("    A redis based cluster cloud system for Minecraft");
        log("    » Discord: https://discord.gg/g2HV52VV4G");
        log("     ");
        info("Starting AutoUpdater...");
        arguments = args;
        boolean debugStart = Boolean.parseBoolean(getArgument("debugStart", "false"));
        info("Start cloud in debug mode: " + debugStart);
        String branch = getArgument("branch", "main");
        info("Downloading cloud from " + branch + " branch");
        String startFile = getArgument("startFile", OSValidator.isWindows() ? "start.bat" : "start.sh");
        if(debugStart) startFile = startFile.split("\\.")[0] + "_debug" + startFile.split("\\.")[1];
        File file = new File(startFile);
        if(!file.exists()){
            info("Cant find start file: " + file.getAbsolutePath());
            Thread.sleep(2500);
            return;
        }
        info("Setting start file to " + startFile);
        String downloadUrl = "https://ci.redicloud.dev/job/redi-cloud/job/" + branch + "/lastSuccessfulBuild/artifact/build/redi-cloud.zip";
        info("Downloading cloud files from " + downloadUrl);
        if(!download(downloadUrl)) return;
        info("Downloading cloud files finished");
        info("Unzipping cloud files");
        ZipUtils.unzipDir(new File("redi-cloud.zip"), ".");
        info("Unzipping cloud files finished");
        info("Deleting zip file");
        new File("redi-cloud.zip").delete();
        info("Deleting zip file finished");
        info("Starting cloud");
        Runtime.getRuntime().exec(startFile);
    }

    private static boolean download(String downloadUrl) {

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                long completeFileSize = httpURLConnection.getContentLengthLong();

                BufferedInputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(new File("redi-cloud.zip"));
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, 1014);
                byte[] data = new byte[1024];
                long downloadedFileSize = 0;
                int x = 0;
                while ((x = inputStream.read(data, 0, 1024)) >= 0) {
                    downloadedFileSize += x;
                    double currentProgress = ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100000d);

                    progress.set(MathUtils.round(currentProgress, 2));

                    bufferedOutputStream.write(data, 0, x);
                }

                bufferedOutputStream.close();
                inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        thread.start();
        while(thread.isAlive() && !thread.isInterrupted()){
            try {
                log(getProgress());
                Thread.sleep(350);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(thread.isInterrupted()){
            info("Error while downloading cloud files!");
            return false;
        }
        return true;
    }

    private static String getProgress() {
        int i = (int) (MathUtils.round(progress.get(), 0) / 10);
        double percent = progress.get();
        int i1 = (int) (percent / 10);
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (int i2 = 1; i2 < 10; i2++) {
            if(i2 <= i1) builder.append("█");
            else builder.append("░");
        }
        builder.append("]");

        return builder.toString();
    }

    private static String getArgument(String key, String defaultValue) {
        for (String argument : arguments) {
            if (argument.startsWith("--" + key + "=")) {
                return argument.split("=")[1];
            }
        }
        return defaultValue;
    }

    private static void info(String message) {
        log("> " + message);
    }

    private static void log(String message) {
        System.out.println(message);
    }

}
