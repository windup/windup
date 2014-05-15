package org.jboss.loom.recog;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.recog.as5.JBossAS5ServerType;
import org.jboss.loom.recog.as7.JBossAS7ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ServerRecognizer {
    private static final Logger log = LoggerFactory.getLogger( ServerRecognizer.class );
    
    //public enum ServerType { JBOSS_AS, TOMCAT, WEBSPHERE, WEBLOGIC, GLASSFISH }; // Should rather be classes, to make it pluginable.
    
    
    /**
     *  Ask all known implementations of IServerType whether their server is in the directory.
     * 
     *  TODO: Return an instance?
     */
    public static IServerType recognizeType( File serverRootDir ) throws MigrationException{
        for( Class<? extends IServerType> typeClass : findServerTypes() ){
            log.debug("    Trying " + typeClass.getSimpleName());
            IServerType type = instantiate(typeClass);
            if( type.isPresentInDir(serverRootDir) )
                return type;
        }
        return null;
    }
    
    /**
     *  Asks given IServerType what version is in the given directory.
     *  TODO: Make method of IServerType?
     *  @deprecated  Use IServerType.recognizeVersion();
     */
    private VersionRange recognizeVersion( Class<? extends IServerType> typeClass, File serverRootDir ) throws MigrationException{
        IServerType type = instantiate( typeClass );
        return type.recognizeVersion( serverRootDir );
        // TODO: Could be called statically?
    }
    
    /**
     *  All-in-one.
     */
    public static ServerInfo recognize( File serverRootDir ) throws MigrationException{
        IServerType type = recognizeType( serverRootDir );
        if( type == null )  return null;
        return new ServerInfo(serverRootDir).setType( type ).setVersionRange( type.recognizeVersion( serverRootDir ) );
    }


    /**
     *  Finds classes implementing IServerType.
     *  Currently static.
     */
    private static Collection<Class<? extends IServerType>> findServerTypes() {
        return (List) Arrays.asList(
                JBossAS5ServerType.class,
                JBossAS7ServerType.class
        );
    }


    /**
     *  Just wraps the potential exception.
     */
    private static IServerType instantiate( Class<? extends IServerType> typeClass ) throws MigrationException {
        try {
            return typeClass.newInstance();
        } catch( InstantiationException | IllegalAccessException ex ) {
            throw new MigrationException("Failed instantiating ServerType "+typeClass.getSimpleName()+": " + ex.getMessage(), ex);
        }
    }
    
}// class
