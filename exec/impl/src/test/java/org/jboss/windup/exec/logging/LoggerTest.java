/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.windup.exec.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class LoggerTest {
    
    @Test
    public void test() {
        Logger log = Logger.getLogger("ZpracujArchiv");
        log.setUseParentHandlers(false);

        Handler h;
        //h = new java.util.logging.ConsoleHandler();
        h = new java.util.logging.StreamHandler(System.out, new SimpleFormatter());
        h.setLevel(Level.ALL);
        //h.setFormatter( new SimpleFormatter() );
        log.addHandler( h );/**/

        try{
          h = new java.util.logging.FileHandler( "Insolvence.log", 50000, 1 );
          h.setLevel(Level.ALL);
          h.setFormatter( new SimpleFormatter() );
          log.addHandler( h );
        }
        catch(Exception ex){ ex.printStackTrace(); }

        log.setLevel(Level.ALL);
        log.entering("Main", "ZpracujArchiv");
        log.severe("Test SEVERE");
        log.info("Test INFO");
        log.log(Level.FINE, "Test FINE");
    }


    @Test
    public void testConfig() {
        /*
        # Handlers    // java.util.logging.ConsoleHandler
        handlers=cz.dynawest.iriswsklient.SystemOutHandler java.util.logging.FileHandler

        # Console
        java.util.logging.ConsoleHandler.formatter = cz.dynawest.iriswsklient.SimplestFormatter

        # File
        java.util.logging.FileHandler.pattern = applicationLog%u.xml
        java.util.logging.FileHandler.formatter = cz.dynawest.iriswsklient.SimplestFormatter


        # Default global logging level.
        .level = FINEST
        Foo.level = WARNING
        Foo.Aj.level = ALL
        */

        System.setProperty("java.util.logging.config.file", "logging.properties");
        try { LogManager.getLogManager().readConfiguration(); }
        catch( Exception ex ){ ex.printStackTrace(); }

        Logger.getLogger("").severe("Root SEVERE");
        Logger.getLogger("").warning("Root WARN");
        Logger.getLogger("Foo").info("Foo INFO");
        Logger.getLogger("Foo").warning("Foo WARN");
        Logger.getLogger("Foo.Aj").info("Foo.Aj INFO");
        Logger.getLogger("Foo.Aj").warning("Foo.Aj WARN");
    }

}
