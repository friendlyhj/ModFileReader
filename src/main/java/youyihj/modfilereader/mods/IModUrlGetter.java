package youyihj.modfilereader.mods;

import java.io.IOException;
import java.util.Optional;

/**
 * @author youyihj
 */
public interface IModUrlGetter extends Comparable<IModUrlGetter> {
    Optional<String> get(ModEntry mod) throws IOException;

    int priority();

    @Override
    default int compareTo(IModUrlGetter o) {
        return Integer.compare(o.priority(), this.priority());
    }
}
