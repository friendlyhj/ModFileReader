package youyihj.modfilereader.mods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author youyihj
 */
public class CommonCNMods implements IModUrlGetter {

    private final Map<String, String> mods = new HashMap<>();

     public CommonCNMods() {
        add("i18nupdatemod", "https://www.mcbbs.net/thread-805273-1-1.html");
        add("JustEnoughCharacters", "https://www.mcbbs.net/thread-639271-1-1.html");
        add("jecharacters", "https://www.mcbbs.net/thread-639271-1-1.html");
        add("CustomSkinLoader", "https://www.mcbbs.net/thread-269807-1-1.html");
        add("NonUpdate-AllMCVersion-Final", "https://www.curseforge.com/minecraft/mc-mods/non-update");
        add("Jade", "https://www.curseforge.com/minecraft/mc-mods/jade");
        add("RoughlyEnoughItems", "https://www.mcbbs.net/thread-1265546-1-1.html");
        add("Flux-Networks", "https://www.curseforge.com/minecraft/mc-mods/flux-networks");
        add("SnowRealMagic", "https://www.mcbbs.net/thread-871191-1-1.html");
        add("Kiwi", "https://www.curseforge.com/minecraft/mc-mods/kiwi");
        add("touhoulittlemaid", "https://www.mcbbs.net/thread-882845-1-1.html");
        add("zenutils", "https://www.curseforge.com/minecraft/mc-mods/zenutil");
        add("TofuCraftReload", "https://www.mcbbs.net/thread-946310-1-1.html");
        add("mmlib", "https://www.curseforge.com/minecraft/mc-mods/mysterious-mountain-lib");
        add("sakura", "https://www.mcbbs.net/thread-895337-1-1.html");
        add("Sakura", "https://www.mcbbs.net/thread-895337-1-1.html");
        add("!mixinbooter", "https://www.curseforge.com/minecraft/mc-mods/mixin-booter");
        add("multiblocked", "https://www.curseforge.com/minecraft/mc-mods/multiblocked");
        add("Multiblocked", "https://www.curseforge.com/minecraft/mc-mods/multiblocked");
    }

    public void add(String mod, String url) {
        mods.put(mod, url);
    }

    public Optional<String> get(ModEntry mod) {
        return Optional.ofNullable(mods.get(mod.getModName()));
    }

    @Override
    public int priority() {
        return 100;
    }
}
