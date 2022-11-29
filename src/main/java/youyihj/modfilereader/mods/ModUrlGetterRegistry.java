package youyihj.modfilereader.mods;

import java.io.IOException;
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

    public Optional<String> readURL(String modName) {
        for (IModUrlGetter getter : getters) {
            try {
                Optional<String> result = getter.get(modName);
                if (result.isPresent()) return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
