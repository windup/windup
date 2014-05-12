package org.jboss.windup.engine.ex;

/**
 *  Root Windup exception to inherit other Windup-specific exceptions from.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupException extends Exception {


    public WindupException( String message ) {
        super( message );
    }


    public WindupException( String message, Throwable cause ) {
        super( message, cause );
    }


    public WindupException( Throwable cause ) {
        super( cause );
    }
    

}// class
