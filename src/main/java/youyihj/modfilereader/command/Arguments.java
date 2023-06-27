package youyihj.modfilereader.command;

import com.beust.jcommander.Parameter;

/**
 * @author youyihj
 */
public class Arguments {
    @Parameter(names = {"-dir", "-modDir"}, description = "The path of mods dictionary")
    private String modDir = "mods";

    @Parameter(names = "-output", description = "The path of output file")
    private String outputTxt = "outputs.txt";

    @Parameter(names = {"-curse", "-all"}, description = "Calls CurseForge and Modrinth API to try to get url of mods, may take much time")
    private boolean all;

    @Parameter(names = "-threads", description = "The count of threads to get url of mods. Max: 20")
    private int threads = 5;

    @Parameter(names = {"-retry", "-retryCount"}, description = "The count of retries gotten for mod links that failed due to network issues")
    private int retryCount = 1;

    @Parameter(names = "-debug", description = "Prints error stack trace")
    private boolean debug;

    public Arguments() {
    }

    public String getModDir() {
        return this.modDir;
    }

    public String getOutputTxt() {
        return this.outputTxt;
    }

    public boolean isAll() {
        return this.all;
    }

    public void setModDir(String modDir) {
        this.modDir = modDir;
    }

    public void setOutputTxt(String outputTxt) {
        this.outputTxt = outputTxt;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public String toString() {
        return "Arguments(modDir=" + this.getModDir() + ", outputTxt=" + this.getOutputTxt() + ", all=" + this.isAll() + ")";
    }

    public int getThreads() {
        return threads < 1 ? 1 : Math.min(threads, 20);
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
