package org.jboss.loom.recog;


import java.io.File;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IServerType {

    public String getDescription();

    /**
     *  Returns (null, null) if it can't recognize the version.
     *  Upper limit may be unset.
     */
    public VersionRange recognizeVersion( File homeDir );

    /**
     *  Returns true if server of this type is detected in the directory (used as a home dir, not searched).
     */
    public boolean isPresentInDir( File serverRootDir );


    /**
     *  Formats a string like "JBoss AS 5.1.0" or "JBoss EAP 5.2.0+" etc.
     *  "STATIC".
     */
    public String format( VersionRange versionRange );

}// class
