package org.jboss.windup.log;

import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.jboss.windup.log.jul.format.SimplestFormatter;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class LoggerTest {

    @Test
    public void testLogging() {
        Logger log = Logger.getLogger( LoggerTest.class.getName() );
        log.setUseParentHandlers(false);

        Handler h;
        //h = new java.util.logging.ConsoleHandler();
        h = new java.util.logging.StreamHandler( System.out, new SimplestFormatter() );
        h.setLevel(Level.ALL);
        //h.setFormatter( new SimpleFormatter() );
        log.addHandler( h );/**/

        try{
            h = new java.util.logging.FileHandler("target/LogTest.log", 50000, 1);
            h.setLevel(Level.ALL);
            h.setFormatter( new SimpleFormatter() );
            log.addHandler( h );
        }
        catch(Exception ex){ ex.printStackTrace(); }

        log.setLevel(Level.ALL);
        log.entering("EnteredClass", "enteredMethod");
        log.severe("Test SEVERE");
        log.info("Test INFO");
        log.log(Level.FINE, "Test FINE");
    }
    
    
    
    @Test
    public void testLoggingProperties() throws Exception {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        try {
            final InputStream is = this.getClass().getResourceAsStream("/logging.properties");
            if( null == is)
                throw new Exception("logging.properties resource not found!");
            LogManager.getLogManager().readConfiguration(is);
        }
        catch( Exception ex ){
            //ex.printStackTrace();
            throw ex;
        }

        Logger.getLogger("").severe("Root SEVERE");
        Logger.getLogger("").warning("Root WARN");
        Logger.getLogger("Foo").info("Foo INFO");
        Logger.getLogger("Foo").warning("Foo WARN");
        Logger.getLogger("Foo.Aj").info("Foo.Aj INFO");
        Logger.getLogger("Foo.Aj").warning("Foo.Aj WARN");
    }
}
