package youyihj.modfilereader.mods;

import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import youyihj.modfilereader.util.JsonUtil;

import java.io.IOException;
import java.util.Optional;

/**
 * @author youyihj
 */
public class ModrinthSearchGetter implements IModUrlGetter {
    private static final String SEARCH_URL = "https://api.modrinth.com/v2/search";
    private static final CloseableHttpClient CLIENT = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(5000).build())
            .build();

    @Override
    public Optional<String> get(ModEntry mod) throws IOException {
        return Optional.ofNullable(readInternal(mod.getModName()));
    }

    public static String readInternal(String name) throws IOException {
        HttpUriRequest request = RequestBuilder.get(SEARCH_URL)
                .addParameter("limit", "1")
                .addParameter("query", name)
                .addParameter("index", "relevance")
                .addParameter("facets", "[[\"project_type:mod\"]]")
                .build();
        return CLIENT.execute(request, (response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
            return getWebUrl(JsonUtil.read(response.getEntity().getContent()));
        }));
    }

    public static String getWebUrl(JsonObject object) {
        String slug = object.getAsJsonArray("hits").get(0).getAsJsonObject().get("slug").getAsString();
        return "https://modrinth.com/mod/" + slug;
    }

    @Override
    public int priority() {
        return 30;
    }
}
