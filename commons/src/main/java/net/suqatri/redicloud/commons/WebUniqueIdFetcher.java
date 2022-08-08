package net.suqatri.redicloud.commons;

import net.suqatri.redicloud.commons.function.Predicates;
import net.suqatri.redicloud.commons.function.future.FutureAction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class is used to fetch a {@link UUID} or a playername (as {@link String}) from a website.
 * This fetcher is used to fetch players that are not in the database.
 * The results will be cached to this service
 */
public class WebUniqueIdFetcher {

    private static final HashMap<String, UUID> uniqueIdCache = new HashMap<>();
    private static final HashMap<UUID, String> nameCache = new HashMap<>();
    private static final Pattern pattern =
            Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    /**
     * Fetches a playername as {@link String} from a website (or from the cache)
     * @param uniqueId the unique id of the player
     * @return
     */
    public static FutureAction<String> fetchName(UUID uniqueId){
        FutureAction<String> futureAction = new FutureAction<>();

        Predicates.notNull(uniqueId, "uniqueId cannot be null", futureAction);

        FutureAction.runAsync(() -> {
            try {
                    if (nameCache.containsKey(uniqueId)) {
                        futureAction.complete(nameCache.get(uniqueId));
                        return;
                    }
                    HttpURLConnection connection = (HttpURLConnection) new URL("https://api.minetools.eu/profile/" + uniqueId.toString()).openConnection();
                    connection.setDoOutput(false);
                    connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
                    );
                    connection.setUseCaches(true);
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String name = fetchLineByName(reader.lines());
                    if (name != null) {
                        nameCache.put(uniqueId, name);
                        uniqueIdCache.put(name, uniqueId);
                        futureAction.complete(name);
                        return;
                    }
                    futureAction.completeExceptionally(new NullPointerException("Could not fetch name"));
            }catch (Exception e){
                futureAction.completeExceptionally(e);
            }
        });

        return futureAction;
    }

    /**
     * Fetches a {@link UUID} from a website (or from the cache)
     * @param name The playername of the player
     * @return
     */
    public static FutureAction<UUID> fetchUniqueId(String name) {
        FutureAction<UUID> futureAction = new FutureAction<>();

        Predicates.notNull(name, "name cannot be null", futureAction);

        FutureAction.runAsync(() -> {
            try {
                if (uniqueIdCache.containsKey(name)) {
                    futureAction.complete(uniqueIdCache.get(name));
                    return;
                }
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + name).openConnection();
                connection.setDoOutput(false);
                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
                );
                connection.setUseCaches(true);
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String uniqueIDString = fetchLineByUUID(reader.lines());
                if (uniqueIDString != null) {
                    UUID uniqueID = UUID.fromString(pattern.matcher(uniqueIDString).replaceAll("$1-$2-$3-$4-$5"));
                    uniqueIdCache.put(name, uniqueID);
                    nameCache.put(uniqueID, name);
                    futureAction.complete(uniqueID);
                    return;
                }
                futureAction.completeExceptionally(new NullPointerException());
            }catch (Exception e){
                futureAction.completeExceptionally(e);
            }
        });

        futureAction.orTimeout(5, TimeUnit.SECONDS);

        return futureAction;
    }

    /**
     * Scan all lines and return the first line that contains the id or null if none is found
     * @param stream
     * @return
     */
    private static String fetchLineByUUID(Stream<String> stream) {
        String line = stream.filter(e -> e.trim().startsWith("\"id\": ")).findFirst().orElse(null);
        return line == null ? null : line
                .replace("\"", "")
                .replace("id: ", "")
                .replace(",", "")
                .trim();
    }

    /**
     * Scan all lines and return the first line that contains the name or null if none is found
     * @param stream
     * @return
     */
    private static String fetchLineByName(Stream<String> stream) {
        String line = stream.filter(e -> e.trim().startsWith("\"name\": ")).findFirst().orElse(null);
        return line == null ? null : line
                .replaceAll("\"", "")
                .replaceAll("name: ", "")
                .replaceAll(",", "")
                .trim();
    }

    /**
     * Cache a {@link UUID} and a playername
     * This will called when a player is entered this service
     * @param name
     * @param uniqueId
     */
    public static void cache(String name, UUID uniqueId){
        nameCache.put(uniqueId, name);
        uniqueIdCache.put(name, uniqueId);
    }

}
