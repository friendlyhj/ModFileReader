package youyihj.modfilereader.mods;

import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import youyihj.modfilereader.util.MurmurHash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author youyihj
 */
public class CurseModGetterByFigurePrint extends CurseModGetter {
    private static final String FIGURE_PRINT_URL = "https://api.curseforge.com/v1/fingerprints";
    private static final String MODS_URL = "https://api.curseforge.com/v1/mods/";

    @Override
    public Optional<String> get(ModEntry mod) throws IOException {
        return Optional.ofNullable(readInternal(getFileFigurePrint(mod.getPath())));
    }

    private static String readInternal(String figurePrint) throws IOException {
        HttpUriRequest request = RequestBuilder.post(FIGURE_PRINT_URL)
                .setEntity(new StringEntity(String.format("{\"fingerprints\": [%s]}", figurePrint), ContentType.APPLICATION_JSON))
                .build();
        Integer projectId = CURSE_FORGE_CLIENT.execute(request, response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
            return getCurseProjectId(toJson(response.getEntity().getContent()));
        });
        return CURSE_FORGE_CLIENT.execute(new HttpGet(MODS_URL + projectId), response -> {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Status: " + response.getStatusLine().getStatusCode());
            }
            return getWebsiteUrl(toJson(response.getEntity().getContent()));
        });
    }

    private static String getFileFigurePrint(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        byte[] bytes = new byte[data.length];
        int index = 0;
        for (byte b : data) {
            if (!((b == 9 || b == 10 || b == 13 || b == 32))) {
                bytes[index++] = b;
            }
        }
        return Integer.toUnsignedString(MurmurHash.hash32(Arrays.copyOf(bytes, index), index, 1));
    }

    private static int getCurseProjectId(JsonObject object) {
        return object.getAsJsonObject("data").getAsJsonArray("exactMatches").get(0).getAsJsonObject().getAsJsonObject("file").get("modId").getAsInt();
    }

    private static String getWebsiteUrl(JsonObject object) {
        return object.getAsJsonObject("data").getAsJsonObject("links").get("websiteUrl").getAsString();
    }

    @Override
    public int priority() {
        return 40;
    }
}
