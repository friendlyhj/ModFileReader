package youyihj.modfilereader.mods;

import java.util.Objects;

/**
 * @author youyihj
 */
public class ModEntry {
    private final String fileName;
    private String modName;
    private String url;

    private ModEntry(String fileName) {
        this.fileName = fileName;
    }

    public static ModEntry of(String fileName) {
        return new ModEntry(fileName);
    }

    public String dump() {
        return url == null ? fileName : String.format("[url=%s]%s[/url]", url, fileName);
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getModName() {
        return this.modName;
    }

    public String getUrl() {
        return this.url;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModEntry modEntry = (ModEntry) o;
        return Objects.equals(fileName, modEntry.fileName) && Objects.equals(modName, modEntry.modName) && Objects.equals(url, modEntry.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, modName, url);
    }

    public String toString() {
        return "ModEntry(fileName=" + this.getFileName() + ", modName=" + this.getModName() + ", url=" + this.getUrl() + ")";
    }
}
