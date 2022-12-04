package youyihj.modfilereader;

import youyihj.modfilereader.command.ModFileReader;

/**
 * @author youyihj
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ModFileReader modFileReader = new ModFileReader(args);
        modFileReader.readMods();
        modFileReader.readURLAndOutput();
    }
}
