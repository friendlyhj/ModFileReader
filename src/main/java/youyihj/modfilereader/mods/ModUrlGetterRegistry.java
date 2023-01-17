package youyihj.modfilereader.mods;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * @author youyihj
 */
public class ModUrlGetterRegistry {

    private final Set<IModUrlGetter> getters = new TreeSet<>();

    public void register(IModUrlGetter getter) {
        getters.add(getter);
    }

    public Optional<String> readURL(ModEntry modEntry, Consumer<ModEntry> failFallback) {
        for (IModUrlGetter getter : getters) {
            try {
                Optional<String> result = getter.get(modEntry);
                if (result.isPresent()) return result;
            } catch (IOException e) {
                failFallback.accept(modEntry);
            } catch (Exception e) {
                System.err.println(modEntry.getFileName());
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
