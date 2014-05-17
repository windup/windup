package org.jboss.windup.engine.decompilers.api;

import org.jboss.windup.engine.decompilers.jad.JadConf;
import org.jboss.windup.engine.decompilers.jad.JadRetroDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JadDecompilerTest extends IDecompilerTestBase
{
    private static final Logger log = LoggerFactory.getLogger(JadDecompilerTest.class);

    @Override
    protected DecompilationConf createConf()
    {
        return new JadConf();
    }

    @Override
    protected Decompiler.Jar getDecompiler()
    {
        return new JadRetroDecompiler();
    }

    @Override
    protected boolean isTestFailedOverExpectations(JarDecompilationResults res)
    {
        return (res.getFailed().size() > 25);
    }

}// class
