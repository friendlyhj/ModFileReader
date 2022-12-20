package youyihj.modfilereader.mods;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author youyihj
 */
public class ModUrlGetterRegistry {

    private final Set<IModUrlGetter> getters = new TreeSet<>();

    public void register(IModUrlGetter getter) {
        getters.add(getter);
    }

    public Optional<String> readURL(ModEntry modEntry) {
        for (IModUrlGetter getter : getters) {
            try {
                Optional<String> result = getter.get(modEntry);
                if (result.isPresent()) return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
