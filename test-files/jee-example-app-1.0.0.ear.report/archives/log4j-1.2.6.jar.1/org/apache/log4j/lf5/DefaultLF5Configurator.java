package org.apache.log4j.lf5;

import org.apache.log4j.spi.LoggerRepository;
import java.net.URL;
import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;

public class DefaultLF5Configurator implements Configurator{
    static /* synthetic */ Class class$org$apache$log4j$lf5$DefaultLF5Configurator;
    public static void configure() throws IOException{
        final String resource="/org/apache/log4j/lf5/config/defaultconfig.properties";
        final URL configFileResource=((DefaultLF5Configurator.class$org$apache$log4j$lf5$DefaultLF5Configurator==null)?(DefaultLF5Configurator.class$org$apache$log4j$lf5$DefaultLF5Configurator=class$("org.apache.log4j.lf5.DefaultLF5Configurator")):DefaultLF5Configurator.class$org$apache$log4j$lf5$DefaultLF5Configurator).getResource(resource);
        if(configFileResource!=null){
            PropertyConfigurator.configure(configFileResource);
            return;
        }
        throw new IOException("Error: Unable to open the resource"+resource);
    }
    public void doConfigure(final URL configURL,final LoggerRepository repository){
        throw new IllegalStateException("This class should NOT be instantiated!");
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
}
