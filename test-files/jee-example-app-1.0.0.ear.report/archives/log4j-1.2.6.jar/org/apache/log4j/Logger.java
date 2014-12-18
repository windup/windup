package org.apache.log4j;

import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Category;

public class Logger extends Category{
    private static final String FQCN;
    static /* synthetic */ Class class$org$apache$log4j$Level;
    protected Logger(final String name){
        super(name);
    }
    public static Logger getLogger(final String name){
        return LogManager.getLogger(name);
    }
    public static Logger getLogger(final Class clazz){
        return LogManager.getLogger(clazz.getName());
    }
    public static Logger getRootLogger(){
        return LogManager.getRootLogger();
    }
    public static Logger getLogger(final String name,final LoggerFactory factory){
        return LogManager.getLogger(name,factory);
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        FQCN=((Logger.class$org$apache$log4j$Level==null)?(Logger.class$org$apache$log4j$Level=class$("org.apache.log4j.Level")):Logger.class$org$apache$log4j$Level).getName();
    }
}
