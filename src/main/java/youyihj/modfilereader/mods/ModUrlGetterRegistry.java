package youyihj.modfilereader.mods;

import youyihj.modfilereader.command.ModFileReader;

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
    private final ModFileReader fileReader;

    public ModUrlGetterRegistry(ModFileReader fileReader) {
        this.fileReader = fileReader;
    }

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
                if (fileReader.getArguments().isDebug()) {
                    System.err.println(modEntry.getFileName());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.err.println(modEntry.getFileName());
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
