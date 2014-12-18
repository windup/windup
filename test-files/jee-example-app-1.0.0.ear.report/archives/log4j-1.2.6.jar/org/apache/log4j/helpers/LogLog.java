package org.apache.log4j.helpers;

import org.apache.log4j.helpers.OptionConverter;

public class LogLog{
    public static final String DEBUG_KEY="log4j.debug";
    public static final String CONFIG_DEBUG_KEY="log4j.configDebug";
    protected static boolean debugEnabled;
    private static boolean quietMode;
    private static final String PREFIX="log4j: ";
    private static final String ERR_PREFIX="log4j:ERROR ";
    private static final String WARN_PREFIX="log4j:WARN ";
    public static void setInternalDebugging(final boolean enabled){
        LogLog.debugEnabled=enabled;
    }
    public static void debug(final String msg){
        if(LogLog.debugEnabled&&!LogLog.quietMode){
            System.out.println("log4j: "+msg);
        }
    }
    public static void debug(final String msg,final Throwable t){
        if(LogLog.debugEnabled&&!LogLog.quietMode){
            System.out.println("log4j: "+msg);
            if(t!=null){
                t.printStackTrace(System.out);
            }
        }
    }
    public static void error(final String msg){
        if(LogLog.quietMode){
            return;
        }
        System.err.println("log4j:ERROR "+msg);
    }
    public static void error(final String msg,final Throwable t){
        if(LogLog.quietMode){
            return;
        }
        System.err.println("log4j:ERROR "+msg);
        if(t!=null){
            t.printStackTrace();
        }
    }
    public static void setQuietMode(final boolean quietMode){
        LogLog.quietMode=quietMode;
    }
    public static void warn(final String msg){
        if(LogLog.quietMode){
            return;
        }
        System.err.println("log4j:WARN "+msg);
    }
    public static void warn(final String msg,final Throwable t){
        if(LogLog.quietMode){
            return;
        }
        System.err.println("log4j:WARN "+msg);
        if(t!=null){
            t.printStackTrace();
        }
    }
    static{
        LogLog.debugEnabled=false;
        LogLog.quietMode=false;
        String key=OptionConverter.getSystemProperty("log4j.debug",null);
        if(key==null){
            key=OptionConverter.getSystemProperty("log4j.configDebug",null);
        }
        if(key!=null){
            LogLog.debugEnabled=OptionConverter.toBoolean(key,true);
        }
    }
}
