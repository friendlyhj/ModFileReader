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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Arguments)) return false;
        final Arguments other = (Arguments) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$modDir = this.getModDir();
        final Object other$modDir = other.getModDir();
        if (this$modDir == null ? other$modDir != null : !this$modDir.equals(other$modDir)) return false;
        final Object this$outputTxt = this.getOutputTxt();
        final Object other$outputTxt = other.getOutputTxt();
        if (this$outputTxt == null ? other$outputTxt != null : !this$outputTxt.equals(other$outputTxt)) return false;
        if (this.isCurse() != other.isCurse()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Arguments;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $modDir = this.getModDir();
        result = result * PRIME + ($modDir == null ? 43 : $modDir.hashCode());
        final Object $outputTxt = this.getOutputTxt();
        result = result * PRIME + ($outputTxt == null ? 43 : $outputTxt.hashCode());
        result = result * PRIME + (this.isCurse() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "Arguments(modDir=" + this.getModDir() + ", outputTxt=" + this.getOutputTxt() + ", curse=" + this.isCurse() + ")";
    }
}
