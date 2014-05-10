package org.jboss.windup.engine.decompilers.api;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DecompilationEx extends Exception {


    public DecompilationEx( String message ) {
        super( message );
    }

    public DecompilationEx( String message, Throwable cause ) {
        super( message, cause );
    }

    public DecompilationEx( Throwable cause ) {
        super( cause );
    }

}// class
