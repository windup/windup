package org.apache.log4j;

import org.apache.log4j.helpers.LogLog;
import java.net.MalformedURLException;
import org.apache.log4j.helpers.Loader;
import java.net.URL;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.RootCategory;
import org.apache.log4j.Level;
import java.util.Enumeration;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;

public class LogManager{
    public static final String DEFAULT_CONFIGURATION_FILE="log4j.properties";
    public static final String DEFAULT_CONFIGURATION_KEY="log4j.configuration";
    public static final String CONFIGURATOR_CLASS_KEY="log4j.configuratorClass";
    public static final String DEFAULT_INIT_OVERRIDE_KEY="log4j.defaultInitOverride";
    private static Object guard;
    private static RepositorySelector repositorySelector;
    public static void setRepositorySelector(final RepositorySelector selector,final Object guard) throws IllegalArgumentException{
        if(LogManager.guard!=null&&LogManager.guard!=guard){
            throw new IllegalArgumentException("Attempted to reset the LoggerFactory without possessing the guard.");
        }
        if(selector==null){
            throw new IllegalArgumentException("RepositorySelector must be non-null.");
        }
        LogManager.guard=guard;
        LogManager.repositorySelector=selector;
    }
    public static LoggerRepository getLoggerRepository(){
        return LogManager.repositorySelector.getLoggerRepository();
    }
    public static Logger getRootLogger(){
        return LogManager.repositorySelector.getLoggerRepository().getRootLogger();
    }
    public static Logger getLogger(final String name){
        return LogManager.repositorySelector.getLoggerRepository().getLogger(name);
    }
    public static Logger getLogger(final Class clazz){
        return LogManager.repositorySelector.getLoggerRepository().getLogger(clazz.getName());
    }
    public static Logger getLogger(final String name,final LoggerFactory factory){
        return LogManager.repositorySelector.getLoggerRepository().getLogger(name,factory);
    }
    public static Logger exists(final String name){
        return LogManager.repositorySelector.getLoggerRepository().exists(name);
    }
    public static Enumeration getCurrentLoggers(){
        return LogManager.repositorySelector.getLoggerRepository().getCurrentLoggers();
    }
    public static void shutdown(){
        LogManager.repositorySelector.getLoggerRepository().shutdown();
    }
    public static void resetConfiguration(){
        LogManager.repositorySelector.getLoggerRepository().resetConfiguration();
    }
    static{
        LogManager.guard=null;
        final Hierarchy h=new Hierarchy(new RootCategory(Level.DEBUG));
        LogManager.repositorySelector=new DefaultRepositorySelector(h);
        final String override=OptionConverter.getSystemProperty("log4j.defaultInitOverride",null);
        if(override==null||"false".equalsIgnoreCase(override)){
            final String resource=OptionConverter.getSystemProperty("log4j.configuration","log4j.properties");
            final String configuratorClassName=OptionConverter.getSystemProperty("log4j.configuratorClass",null);
            URL url=null;
            try{
                url=new URL(resource);
            }
            catch(MalformedURLException ex){
                url=Loader.getResource(resource);
            }
            if(url!=null){
                LogLog.debug("Using URL ["+url+"] for automatic log4j configuration.");
                OptionConverter.selectAndConfigure(url,configuratorClassName,getLoggerRepository());
            }
            else{
                LogLog.debug("Could not find resource: ["+resource+"].");
            }
        }
    }
}
