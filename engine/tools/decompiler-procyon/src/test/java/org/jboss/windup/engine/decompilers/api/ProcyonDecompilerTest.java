package org.jboss.windup.engine.decompilers.api;

import org.jboss.windup.engine.decompilers.procyon.ProcyonConf;
import org.jboss.windup.engine.decompilers.procyon.ProcyonDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ProcyonDecompilerTest extends IDecompilerTestBase {
    private static final Logger log = LoggerFactory.getLogger( ProcyonDecompilerTest.class );
    
    @Override
    protected DecompilationConf createConf() {
        return new ProcyonConf();
    }


    @Override
    protected IDecompiler.Jar getDecompiler() {
        return new ProcyonDecompiler();
    }


    @Override
    protected boolean isTestFailedOverExpectations( JarDecompilationResults res ) {
        return ( res.getFailed().size() > 1 );
    }

}// class
