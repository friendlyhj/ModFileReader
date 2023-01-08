package youyihj.modfilereader.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author youyihj
 */
public class JsonUtil {
    public static JsonObject read(InputStream inputStream) {
        return JsonParser.parseReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))).getAsJsonObject();
    }
}
