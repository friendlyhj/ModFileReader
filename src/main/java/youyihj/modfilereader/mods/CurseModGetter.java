package youyihj.modfilereader.mods;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.jar.JarFile;

/**
 * @author youyihj
 */
public abstract class CurseModGetter implements IModUrlGetter {
    protected static final CloseableHttpClient CURSE_FORGE_CLIENT;

    static {
        String key = System.getenv("CURSE_API_KEY");
        if (key == null) {
            try {
                JarFile jar = new JarFile(new File(CurseModGetterBySlug.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
                key = jar.getManifest().getMainAttributes().getValue("CurseForge-ApiKey");
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Failed to read curse api key", e);
            }
        }
        CURSE_FORGE_CLIENT = HttpClientBuilder.create()
                .setDefaultHeaders(Arrays.asList(
                        new BasicHeader("Accept", "application/json"),
                        new BasicHeader("x-api-key", key)
                ))
                .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(5000).build())
                .build();
    }
}
