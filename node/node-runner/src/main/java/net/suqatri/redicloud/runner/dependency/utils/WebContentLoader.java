package net.suqatri.redicloud.runner.dependency.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebContentLoader {

    public static String loadContent(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0");
            connection.connect();
            int code = connection.getResponseCode();
            if (code != 200) {
                connection.disconnect();
                return null;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            bufferedReader.lines().forEach(s -> builder.append(s));
            bufferedReader.close();
            connection.disconnect();
            return builder.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
