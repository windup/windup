package org.jboss.windup.decompiler.procyon;

import java.util.EnumSet;

import com.strobel.decompiler.DecompilerSettings;

/**
 * Procyon-specific configuration.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonConfiguration
{
    private DecompilerSettings decompilerSettings = new DecompilerSettings();

    private EnumSet<LineNumberFormatter.LineNumberOption> lineNumberOptions = EnumSet
                .noneOf(LineNumberFormatter.LineNumberOption.class);

    private boolean includeNested = true;

    public boolean isIncludeNested()
    {
        return includeNested;
    }

    public ProcyonConfiguration setIncludeNested(boolean includeNested)
    {
        this.includeNested = includeNested;
        return this;
    }

    public DecompilerSettings getDecompilerSettings()
    {
        return decompilerSettings;
    }

    public ProcyonConfiguration setDecompilerSettings(DecompilerSettings decompilerSettings)
    {
        this.decompilerSettings = decompilerSettings;
        return this;
    }

    public EnumSet<LineNumberFormatter.LineNumberOption> getLineNumberOptions()
    {
        return lineNumberOptions;
    }

    public ProcyonConfiguration setLineNumberOptions(EnumSet<LineNumberFormatter.LineNumberOption> lineNumberOptions)
    {
        this.lineNumberOptions = lineNumberOptions;
        return this;
    }

}
