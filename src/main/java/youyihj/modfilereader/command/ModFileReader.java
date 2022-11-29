package youyihj.modfilereader.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.internal.DefaultConsole;
import youyihj.modfilereader.mods.CommonCNMods;
import youyihj.modfilereader.mods.CurseModGetter;
import youyihj.modfilereader.mods.ModEntry;
import youyihj.modfilereader.mods.ModUrlGetterRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ModFileReader {
    private final JCommander commander;
    private final Arguments arguments;
    private final List<ModEntry> mods = new ArrayList<>();

    private final ModUrlGetterRegistry getterRegistry = new ModUrlGetterRegistry();

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public ModFileReader(String[] args) {
        this.arguments = new Arguments();
        this.commander = JCommander.newBuilder().args(args).addObject(arguments).console(new DefaultConsole(System.out)).build();
        init();
    }

    public synchronized void readMods() throws IOException {
        Files.walkFileTree(Paths.get(arguments.getModDir()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".jar")) {
                    mods.add(ModEntry.of(fileName));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.endsWith("memory_repo")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public String getModName(String fileName) {
        // removes [xxx] prefix
        if (fileName.startsWith("[")) {
            int index = fileName.indexOf("]");
            if (index != -1) {
                fileName = fileName.substring(index + 1);
            }
        }
        String[] split = fileName.split("[-+_ ]");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            if (s.contains(".")) break; // version
            sb.append(s);
        }
        return sb.toString();
    }

    public void readURL() {
        for (ModEntry mod : mods) {
            executor.submit(new ReadURLCaller(mod));
        }
        executor.shutdown();
    }

    public synchronized void output() throws IOException, InterruptedException {
        while (!executor.isTerminated()) {
            this.wait(1000);
        }
        Files.write(Paths.get(arguments.getOutputTxt()), mods.stream().map(ModEntry::dump).collect(Collectors.toList()), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    private void init() {
        getterRegistry.register(new CommonCNMods());
        if (arguments.isCurse()) {
            getterRegistry.register(new CurseModGetter(s -> CurseModGetter.spiltBy(s, ""), 50));
            getterRegistry.register(new CurseModGetter(s -> CurseModGetter.spiltBy(s, "-"), 49));
        }
    }

    public JCommander getCommander() {
        return this.commander;
    }

    public Arguments getArguments() {
        return this.arguments;
    }

    public List<ModEntry> getMods() {
        return this.mods;
    }

    private class ReadURLCaller implements Callable<Void> {
        private final ModEntry mod;

        public ReadURLCaller(ModEntry modEntry) {
            this.mod = modEntry;
        }

        @Override
        public Void call() {
            commander.getConsole().println("Reading URL for " + mod.getFileName());
            mod.setModName(getModName(mod.getFileName()));
            String modName = mod.getModName();
            getterRegistry.readURL(modName).ifPresent(mod::setUrl);
            return null;
        }
    }
}
