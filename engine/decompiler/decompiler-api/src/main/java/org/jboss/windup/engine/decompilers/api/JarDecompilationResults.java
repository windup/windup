package org.jboss.windup.engine.decompilers.api;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *  Keeps a count of successful decompilations
 *  and list of failed ones,
 *  in the form of an exception with String path and cause exception.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JarDecompilationResults {

    private int decompiledCount = 0;
    
    private List<DecompilationPathException> failed = new LinkedList();

    
    public void addDecompiled( String path ){
        this.decompiledCount++;
    }
    
    public void addFailed( DecompilationPathException ex ){
        this.failed.add( ex );
    }
    

    public List<DecompilationPathException> getFailed() {
        return Collections.unmodifiableList( this.failed );
    }


    public int getDecompiledCount() {
        return this.decompiledCount;
    }
    
}// class
