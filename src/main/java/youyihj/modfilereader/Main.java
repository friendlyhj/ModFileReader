package youyihj.modfilereader;

import youyihj.modfilereader.command.ModFileReader;

/**
 * @author youyihj
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ModFileReader modFileReader = ModFileReader.withArgs(args);
        if (modFileReader != null) {
            modFileReader.readMods();
            modFileReader.readURLAndOutput();
        }
    }
}
