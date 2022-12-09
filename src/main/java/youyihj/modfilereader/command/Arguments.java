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

    @Parameter(names = "-curse", description = "Calls CurseForge API to try to get url of mods, may take much time")
    private boolean curse;

    @Parameter(names = "-threads", description = "The count of threads to get url of mods. Max: 20")
    private int threads = 5;

    public Arguments() {
    }

    public String getModDir() {
        return this.modDir;
    }

    public String getOutputTxt() {
        return this.outputTxt;
    }

    public boolean isCurse() {
        return this.curse;
    }

    public void setModDir(String modDir) {
        this.modDir = modDir;
    }

    public void setOutputTxt(String outputTxt) {
        this.outputTxt = outputTxt;
    }

    public void setCurse(boolean curse) {
        this.curse = curse;
    }

    public String toString() {
        return "Arguments(modDir=" + this.getModDir() + ", outputTxt=" + this.getOutputTxt() + ", curse=" + this.isCurse() + ")";
    }

    public int getThreads() {
        return threads < 1 ? 1 : Math.min(threads, 20);
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
