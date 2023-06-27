package youyihj.modfilereader.mods;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import youyihj.modfilereader.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author youyihj
 */
public class ModrinthFileHashGetter implements IModUrlGetter {
    private static final String VERSION_FILE_URL = "https://api.modrinth.com/v2/version_file/";
    private static final String PROJECT_URL = "https://api.modrinth.com/v2/project/";

    private static final CloseableHttpClient CLIENT = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(5000).build())
            .setDefaultHeaders(Arrays.asList(
                    new BasicHeader("Accept", "application/json"),
                    new BasicHeader("User-Agent", "friendlyhj/ModFileReader")
            ))
            .build();

    @Override
    public Optional<String> get(ModEntry mod) throws IOException {
        return Optional.ofNullable(readInternal(sha1(mod.getPath())));
    }

    public static String readInternal(String sha1) throws IOException {
        HttpUriRequest request = RequestBuilder.get(VERSION_FILE_URL + sha1).build();
        String projectId = CLIENT.execute(request, (response -> {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Status: " + statusCode);
            }
            return getProjectId(JsonUtil.read(response.getEntity().getContent()));
        }));
        if (projectId == null) {
            return null;
        }
        HttpUriRequest slugRequest = RequestBuilder.get(PROJECT_URL + projectId).build();
        return CLIENT.execute(slugRequest, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
            return getProjectUrl(JsonUtil.read(response.getEntity().getContent()));
        });
    }

    public static String sha1(Path path) throws IOException {
        MessageDigest sha1 = DigestUtils.getSha1Digest();
        sha1.update(Files.readAllBytes(path));
        return Hex.encodeHexString(sha1.digest());
    }

    public static String getProjectId(JsonObject object) {
        return object.get("project_id").getAsString();
    }

    public static String getProjectUrl(JsonObject object) {
        return "https://modrinth.com/mod/" + object.get("slug").getAsString();
    }

    @Override
    public int priority() {
        return 30;
    }
}
