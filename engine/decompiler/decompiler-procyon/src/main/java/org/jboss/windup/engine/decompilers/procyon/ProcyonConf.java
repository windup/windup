package org.jboss.windup.engine.decompilers.procyon;

import java.util.EnumSet;

import org.jboss.windup.engine.decompilers.api.DecompilationConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.LineNumberFormatter;

/**
 * Procyon-specific configuration.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ProcyonConf extends DecompilationConf
{
    private static final Logger log = LoggerFactory.getLogger(ProcyonConf.class);

    private DecompilerSettings decompilerSettings = new DecompilerSettings();

    private EnumSet<LineNumberFormatter.LineNumberOption> lineNumberOptions = EnumSet
                .noneOf(LineNumberFormatter.LineNumberOption.class);

    private boolean includeNested = true;

    public boolean isIncludeNested()
    {
        return includeNested;
    }

    public void setIncludeNested(boolean includeNested)
    {
        this.includeNested = includeNested;
    }

    public DecompilerSettings getDecompilerSettings()
    {
        return decompilerSettings;
    }

    public void setDecompilerSettings(DecompilerSettings decompilerSettings)
    {
        this.decompilerSettings = decompilerSettings;
    }

    public EnumSet<LineNumberFormatter.LineNumberOption> getLineNumberOptions()
    {
        return lineNumberOptions;
    }

    public void setLineNumberOptions(EnumSet<LineNumberFormatter.LineNumberOption> lineNumberOptions)
    {
        this.lineNumberOptions = lineNumberOptions;
    }

}// class
