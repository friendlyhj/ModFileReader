package youyihj.modfilereader.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.DefaultConsole;
import youyihj.modfilereader.mods.*;

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
    private static final Pattern MOD_NAME_REGEX = Pattern.compile("([a-zA-Z0-9][\\w\\-+_' ]+)[\\-+_ ]");

    private final JCommander commander;
    private final Arguments arguments;
    private final List<ModEntry> mods = new ArrayList<>();

    private final ModUrlGetterRegistry getterRegistry = new ModUrlGetterRegistry();

    private ModFileReader(JCommander commander, Arguments arguments) {
        this.commander = commander;
        this.arguments = arguments;
    }

    public static ModFileReader withArgs(String[] args) {
        try {
            Arguments arguments = new Arguments();
            ModFileReader reader = new ModFileReader(JCommander.newBuilder().args(args).addObject(arguments).programName("<file name>").console(new DefaultConsole(System.out)).build(), arguments);
            reader.init();
            return reader;
        } catch (ParameterException e) {
            e.usage();
            return null;
        }
    }

    public void readMods() throws IOException {
        Files.walkFileTree(Paths.get(arguments.getModDir()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".jar")) {
                    mods.add(ModEntry.of(file));
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
        Matcher matcher = MOD_NAME_REGEX.matcher(fileName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fileName;
    }

    public void readURLAndOutput() {
        ExecutorService executor = Executors.newFixedThreadPool(arguments.getThreads());
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
            getterRegistry.register(new CurseModGetterBySlug(s -> CurseModGetterBySlug.spiltBy(s, ""), 50));
            getterRegistry.register(new CurseModGetterBySlug(s -> CurseModGetterBySlug.spiltBy(s, "-"), 49));
            getterRegistry.register(new CurseModGetterByFigurePrint());
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
            getterRegistry.readURL(mod).ifPresent(mod::setUrl);
        }
    }
}
