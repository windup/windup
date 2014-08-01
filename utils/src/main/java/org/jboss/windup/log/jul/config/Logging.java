package org.jboss.windup.log.jul.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.jboss.windup.log.jul.format.SingleLineFormatter;
import org.jboss.windup.log.jul.format.SystemOutHandler;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Logging {
    static{
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s %6$s%n");
        System.out.println( "java.util.logging.SimpleFormatter.format was set." );
    }
    
    private static boolean initDone = false;

    /** init() helper. */
    public static void init()
    {
        if( initDone )
            return;
        initDone = true;
        
        Logger.getLogger("").getHandlers()[0].setFormatter( new SingleLineFormatter() );
        Logger.getLogger("org.jboss.forge").setLevel(Level.WARNING);
        Logger.getLogger("org.jboss.weld").setLevel(Level.WARNING);

        String logConfigFile = System.getProperty("java.util.logging.config.file");
        if( logConfigFile != null ){
            System.out.println("Reading logging config from: " + logConfigFile);
            System.out.println("(Set in sys prop java.util.logging.config.file)");
        }
        else {
            logConfigFile = "logging.properties";
            System.out.println("Reading logging config from default location: " + logConfigFile);
            System.out.println("(Can be set in sys prop java.util.logging.config.file)");
        }
        
        InputStream is = null;
        try{
            is = new FileInputStream(logConfigFile);
        }
        catch(FileNotFoundException ex) {
            System.out.println("Not found, trying resource /logging.properties");
            is = Logging.class.getResourceAsStream("/logging.properties");
            if( null == is ){
                useDefault("Resource /logging.properties not found.");
                return;
            }
        }
        LogManager.getLogManager().reset();
        try {
            LogManager.getLogManager().readConfiguration(is);
            System.out.println("Read successfully.");
        } catch( Exception ex ) {
            useDefault( ex.getMessage() );
            return;
        }

        Logger.getLogger(Logging.class.getName()).info("Logging configured.");
    }
    
    private static void useDefault( String reason ){
        System.err.println( reason + " Using hard-coded default logging config.");
        final SystemOutHandler soutHandler = new SystemOutHandler();
        soutHandler.setFormatter( new SingleLineFormatter() );
        //Logger.getLogger("").addHandler( soutHandler );
        Logger.getLogger("org.jboss.forge").setLevel(Level.WARNING);
    }


    public static Logger of( Class cls ) {
        return Logger.getLogger(cls.getName());
    }
}// class
