package youyihj.modfilereader.mods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class CurseModGetter implements IModUrlGetter {
    private static final String SEARCH_URL = "https://api.curseforge.com/v1/mods/search";
    private static final String API_KEY;
    private static final int MINECRAFT_GAME_ID = 432;
    private static final int SORT_BY_NAME = 4;
    private static final String DESCENDING = "desc";
    private static final CloseableHttpClient CLIENT;
    private static final Gson GSON = new GsonBuilder().create();
    private final Function<String, String> slugProcessor;
    private final int priority;

    static {
        String key = System.getenv("CURSE_API_KEY");
        if (key == null) {
            try {
                JarFile jar = new JarFile(new File(CurseModGetter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
                key = jar.getManifest().getMainAttributes().getValue("CurseForge-ApiKey");
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Failed to read curse api key", e);
            }
        }
        API_KEY = key;
        CLIENT = HttpClientBuilder.create()
                .setDefaultHeaders(Arrays.asList(
                        new BasicHeader("Accept", "application/json"),
                        new BasicHeader("x-api-key", API_KEY)
                ))
                .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(5000).build())
                .build();
    }

    public CurseModGetter(Function<String, String> slugProcessor, int priority) {
        this.slugProcessor = slugProcessor;
        this.priority = priority;
    }

    @Override
    public Optional<String> get(String modName) throws IOException {
        return Optional.ofNullable(readInternal(slugProcessor.apply(modName)));
    }

    @Override
    public int priority() {
        return priority;
    }

    public static String spiltBy(String s, String splitStr) {
        StringBuilder sb = new StringBuilder(s.substring(0, 1));
        for (int i = 1; i < s.length(); i++) {
            char charAt = s.charAt(i);
            if (Character.isUpperCase(charAt)) {
                sb.append(splitStr);
            }
            if (Character.isLetterOrDigit(charAt)) {
                sb.append(charAt);
            }
        }
        return sb.toString().toLowerCase(Locale.ENGLISH);
    }

    private static String readInternal(String modSlug) throws IOException {
        return CLIENT.execute(new HttpGet(buildSearchURL(modSlug)), response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
//            return new String(IOUtils.readAllBytes(response.getEntity().getContent()), StandardCharsets.UTF_8);
            return readWebLink(GSON.newJsonReader(new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))));
        });
    }

    public static String buildSearchURL(String modName) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("slug", modName);
        parameters.put("gameId", String.valueOf(MINECRAFT_GAME_ID));
        parameters.put("ModsSearchSortField", String.valueOf(SORT_BY_NAME));
        parameters.put("sortOrder", DESCENDING);
        parameters.put("index", String.valueOf(0));
        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&", SEARCH_URL + "?", ""));
    }

    public static String readWebLink(JsonReader jsonReader) throws IOException {
        String result = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String s = jsonReader.nextName();
            if (s.equals("data")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String s1 = jsonReader.nextName();
                        if (s1.equals("links")) {
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                String s2 = jsonReader.nextName();
                                if (s2.equals("websiteUrl")) {
                                    if (result == null) {
                                        result = jsonReader.nextString();
                                    } else {
                                        jsonReader.skipValue();
                                    }
                                    if (!result.contains("mc-mods")) {
                                        result = null;
                                    }
                                } else {
                                    jsonReader.skipValue();
                                }
                            }
                            jsonReader.endObject();
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                }
                jsonReader.endArray();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return result;
    }
}
