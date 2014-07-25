package org.jboss.windup.log.jul.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /** init() helper. */
    public static void init()
    {        
        Logger.getLogger("").getHandlers()[0].setFormatter( new SingleLineFormatter() );

        String logConfigFile = System.getProperty("java.util.logging.config.file", "logging.properties");
        try
        {
            final InputStream is;
            if( logConfigFile != null ){
                System.out.println("Reading logging config from: " + logConfigFile);
                System.out.println("(Set in sys prop java.util.logging.config.file)");
                is = new FileInputStream(logConfigFile);
            }
            else {
                System.out.println("Reading logging config from resource /logging.properties");
                is = Logging.class.getResourceAsStream("/logging.properties");
                if( null == is)
                    //throw new Exception("logging.properties resource not found!");
                    System.err.println("logging.properties resource not found!");
                    
            }
            LogManager.getLogManager().readConfiguration(is);
        }
        catch(IOException ex)
        {
            System.err.println("Can't read logging config from ["+logConfigFile+"]. Using default.");
            final SystemOutHandler soutHandler = new SystemOutHandler();
            soutHandler.setFormatter( new SingleLineFormatter() );
            Logger.getLogger("").addHandler( soutHandler );
        }
        Logger.getLogger(Logging.class.getName()).info("Logging configured.");
    }
}// class
