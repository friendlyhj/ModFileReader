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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final List<ModEntry> retryMods = new CopyOnWriteArrayList<>();
    private int size = 0;
    private final AtomicInteger index = new AtomicInteger();

    private final ModUrlGetterRegistry getterRegistry = new ModUrlGetterRegistry(this);

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
                    size += 1;
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
        CompletableFuture<Void> run = readModUrl(mods, executor);
        if (arguments.isAll()) {
            for (int i = 0; i < arguments.getRetryCount(); i++) {
                run.thenAccept(result -> {
                    size = retryMods.size();
                    index.set(0);
                }).thenAcceptAsync(result -> {
                    if (!retryMods.isEmpty()) {
                        commander.getConsole().println("Failed to get url for " + size + " mods. Try again...");
                        readModUrl(retryMods, executor);
                    }
                });
            }
        }
        run.thenAccept(it -> {
            output();
            executor.shutdown();
        });
    }

    private CompletableFuture<Void> readModUrl(List<ModEntry> entries, Executor executor) {
        return CompletableFuture.allOf(
                entries.stream()
                        .map(ReadURLTask::new)
                        .map(caller -> CompletableFuture.runAsync(caller, executor))
                        .toArray(CompletableFuture[]::new)
        );
    }

    public void output() {
        commander.getConsole().println("Successful!");
        try {
            Files.write(Paths.get(arguments.getOutputTxt()), mods.stream().map(ModEntry::dump).collect(Collectors.toList()), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        getterRegistry.register(new CommonCNMods());
        if (arguments.isAll()) {
            getterRegistry.register(new CurseModGetterBySlug(s -> CurseModGetterBySlug.spiltBy(s, ""), 50));
            getterRegistry.register(new CurseModGetterBySlug(s -> CurseModGetterBySlug.spiltBy(s, "-"), 49));
            getterRegistry.register(new CurseModGetterByFigurePrint());
            getterRegistry.register(new ModrinthFileHashGetter());
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
            commander.getConsole().println("Reading URL for " + mod.getFileName() + " (" + index.incrementAndGet() + "/" + size + ")");
            mod.setModName(getModName(mod.getFileName()));
            getterRegistry.readURL(mod, retryMods::add).ifPresent(mod::setUrl);
        }
    }
}
