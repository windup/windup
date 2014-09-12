package org.jboss.windup.decompilers.api;

import org.jboss.windup.decompilers.procyon.CfrConf;
import org.jboss.windup.decompilers.procyon.CfrDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CfrDecompilerTest extends DecompilerTestBase
{
    private static final Logger log = LoggerFactory.getLogger(CfrDecompilerTest.class);

    @Override
    protected DecompilationConf createConf()
    {
        return new CfrConf();
    }

    @Override
    protected Decompiler.Jar getDecompiler()
    {
        return new CfrDecompiler();
    }

    @Override
    protected boolean isTestFailedOverExpectations(JarDecompilationResults res)
    {
        return (res.getFailed().size() > 50);
    }

}// class
