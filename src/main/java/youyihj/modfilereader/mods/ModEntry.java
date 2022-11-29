package youyihj.modfilereader.mods;

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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ModEntry)) return false;
        final ModEntry other = (ModEntry) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$fileName = this.getFileName();
        final Object other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) return false;
        final Object this$modName = this.getModName();
        final Object other$modName = other.getModName();
        if (this$modName == null ? other$modName != null : !this$modName.equals(other$modName)) return false;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ModEntry;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $fileName = this.getFileName();
        result = result * PRIME + ($fileName == null ? 43 : $fileName.hashCode());
        final Object $modName = this.getModName();
        result = result * PRIME + ($modName == null ? 43 : $modName.hashCode());
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        return result;
    }

    public String toString() {
        return "ModEntry(fileName=" + this.getFileName() + ", modName=" + this.getModName() + ", url=" + this.getUrl() + ")";
    }
}
