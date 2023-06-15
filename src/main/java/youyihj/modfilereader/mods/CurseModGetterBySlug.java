package youyihj.modfilereader.mods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import youyihj.modfilereader.util.JsonUtil;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author youyihj
 */
public class CurseModGetterBySlug extends CurseModGetter {
    private static final String SEARCH_URL = "https://api.curseforge.com/v1/mods/search";
    private static final int MINECRAFT_GAME_ID = 432;
    private static final int SORT_BY_NAME = 4;
    private static final String DESCENDING = "desc";
    private final Function<String, String> slugProcessor;
    private final int priority;

    public CurseModGetterBySlug(Function<String, String> slugProcessor, int priority) {
        this.slugProcessor = slugProcessor;
        this.priority = priority;
    }

    @Override
    public Optional<String> get(ModEntry mod) throws IOException {
        return Optional.ofNullable(readInternal(slugProcessor.apply(mod.getModName())));
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
        return CURSE_FORGE_CLIENT.execute(buildSearchRequest(modSlug), response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
//            return new String(IOUtils.readAllBytes(response.getEntity().getContent()), StandardCharsets.UTF_8);
            return getWebLink(JsonUtil.read(response.getEntity().getContent()));
        });
    }

    public static HttpUriRequest buildSearchRequest(String modName) {
        return RequestBuilder.get(SEARCH_URL)
                .addParameter("slug", modName)
                .addParameter("gameId", String.valueOf(MINECRAFT_GAME_ID))
                .addParameter("ModsSearchSortField", String.valueOf(SORT_BY_NAME))
                .addParameter("sortOrder", DESCENDING)
                .addParameter("index", "0")
                .build();
    }

    public static String getWebLink(JsonObject jsonObject) {
        for (JsonElement data : jsonObject.getAsJsonArray("data")) {
            String result = data.getAsJsonObject().getAsJsonObject("links").get("websiteUrl").getAsString();
            if (result.contains("mc-mods")) {
                return result;
            }
        }
        return null;
    }
}
