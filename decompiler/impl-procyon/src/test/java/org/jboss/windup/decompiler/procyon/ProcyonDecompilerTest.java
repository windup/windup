package org.jboss.windup.decompiler.procyon;

import org.jboss.windup.decompiler.DecompilerTestBase;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompilerTest extends DecompilerTestBase
{
    @Override
    protected Decompiler getDecompiler()
    {
        return new ProcyonDecompiler();
    }

    @Override
    protected boolean isResultValid(DecompilationResult res)
    {
        return (res.getFailures().size() == 1);
    }

}
