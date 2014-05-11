package org.jboss.windup.engine.decompilers.api;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DecompilationPathEx extends DecompilationEx {

    private final String path;
    

    public DecompilationPathEx( String message, String path ) {
        super( message );
        this.path = path;
    }

    public DecompilationPathEx( String message, String path, Throwable cause ) {
        super( message, cause );
        this.path = path;
    }

    public DecompilationPathEx( String path, Throwable cause ) {
        super( cause );
        this.path = path;        
    }


    public String getPath() {
        return path;
    }
    
}// class
