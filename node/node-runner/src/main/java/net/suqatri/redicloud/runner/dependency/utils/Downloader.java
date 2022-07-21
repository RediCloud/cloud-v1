package net.suqatri.redicloud.runner.dependency.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Downloader {

    public static void userAgentDownload(String url, File file) throws IOException {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0");
        connection.connect();
        Files.copy(connection.getInputStream(), Paths.get(file.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
    }

}
