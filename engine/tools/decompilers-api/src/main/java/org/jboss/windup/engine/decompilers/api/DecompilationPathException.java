package org.jboss.windup.engine.decompilers.api;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DecompilationPathException extends DecompilationException {

    private final String path;
    

    public DecompilationPathException( String message, String path ) {
        super( message );
        this.path = path;
    }

    public DecompilationPathException( String message, String path, Throwable cause ) {
        super( message, cause );
        this.path = path;
    }

    public DecompilationPathException( String path, Throwable cause ) {
        super( cause );
        this.path = path;        
    }


    public String getPath() {
        return path;
    }
    
}// class
