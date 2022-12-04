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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class ModFileReader {
    private final JCommander commander;
    private final Arguments arguments;
    private final List<ModEntry> mods = new ArrayList<>();

    private final ModUrlGetterRegistry getterRegistry = new ModUrlGetterRegistry();

    public ModFileReader(String[] args) {
        this.arguments = new Arguments();
        this.commander = JCommander.newBuilder().args(args).addObject(arguments).console(new DefaultConsole(System.out)).build();
        init();
    }

    public void readMods() throws IOException {
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
        Pattern pattern = Pattern.compile("([\\w\\-+_' ]+)[-+_ ]");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fileName;
    }

    public void readURLAndOutput() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<Void> run = CompletableFuture.allOf(
                mods.stream()
                        .map(ReadURLTask::new)
                        .map(caller -> CompletableFuture.runAsync(caller, executor))
                        .toArray(CompletableFuture[]::new)
        );
        run.thenAccept((result) -> this.output());
        executor.shutdown();
    }

    public void output() {
        try {
            Files.write(Paths.get(arguments.getOutputTxt()), mods.stream().map(ModEntry::dump).collect(Collectors.toList()), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private class ReadURLTask implements Runnable {
        private final ModEntry mod;

        public ReadURLTask(ModEntry modEntry) {
            this.mod = modEntry;
        }

        @Override
        public void run() {
            commander.getConsole().println("Reading URL for " + mod.getFileName());
            mod.setModName(getModName(mod.getFileName()));
            String modName = mod.getModName();
            getterRegistry.readURL(modName).ifPresent(mod::setUrl);
        }
    }
}
