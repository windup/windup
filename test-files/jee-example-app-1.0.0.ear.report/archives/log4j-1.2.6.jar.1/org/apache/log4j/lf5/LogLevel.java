package org.apache.log4j.lf5;

import java.util.HashMap;
import java.util.Arrays;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.lf5.LogLevelFormatException;
import java.util.Map;
import java.io.Serializable;

public class LogLevel implements Serializable{
    public static final LogLevel FATAL;
    public static final LogLevel ERROR;
    public static final LogLevel WARN;
    public static final LogLevel INFO;
    public static final LogLevel DEBUG;
    public static final LogLevel SEVERE;
    public static final LogLevel WARNING;
    public static final LogLevel CONFIG;
    public static final LogLevel FINE;
    public static final LogLevel FINER;
    public static final LogLevel FINEST;
    protected String _label;
    protected int _precedence;
    private static LogLevel[] _log4JLevels;
    private static LogLevel[] _jdk14Levels;
    private static LogLevel[] _allDefaultLevels;
    private static Map _logLevelMap;
    private static Map _logLevelColorMap;
    private static Map _registeredLogLevelMap;
    public LogLevel(final String label,final int precedence){
        super();
        this._label=label;
        this._precedence=precedence;
    }
    public String getLabel(){
        return this._label;
    }
    public boolean encompasses(final LogLevel level){
        return level.getPrecedence()<=this.getPrecedence();
    }
    public static LogLevel valueOf(String level) throws LogLevelFormatException{
        LogLevel logLevel=null;
        if(level!=null){
            level=level.trim().toUpperCase();
            logLevel=LogLevel._logLevelMap.get(level);
        }
        if(logLevel==null&&LogLevel._registeredLogLevelMap.size()>0){
            logLevel=LogLevel._registeredLogLevelMap.get(level);
        }
        if(logLevel==null){
            final StringBuffer buf=new StringBuffer();
            buf.append("Error while trying to parse ("+level+") into");
            buf.append(" a LogLevel.");
            throw new LogLevelFormatException(buf.toString());
        }
        return logLevel;
    }
    public static LogLevel register(final LogLevel logLevel){
        if(logLevel==null){
            return null;
        }
        if(LogLevel._logLevelMap.get(logLevel.getLabel())==null){
            return LogLevel._registeredLogLevelMap.put(logLevel.getLabel(),logLevel);
        }
        return null;
    }
    public static void register(final LogLevel[] logLevels){
        if(logLevels!=null){
            for(int i=0;i<logLevels.length;++i){
                register(logLevels[i]);
            }
        }
    }
    public static void register(final List logLevels){
        if(logLevels!=null){
            final Iterator it=logLevels.iterator();
            while(it.hasNext()){
                register(it.next());
            }
        }
    }
    public boolean equals(final Object o){
        boolean equals=false;
        if(o instanceof LogLevel&&this.getPrecedence()==((LogLevel)o).getPrecedence()){
            equals=true;
        }
        return equals;
    }
    public int hashCode(){
        return this._label.hashCode();
    }
    public String toString(){
        return this._label;
    }
    public void setLogLevelColorMap(final LogLevel level,Color color){
        LogLevel._logLevelColorMap.remove(level);
        if(color==null){
            color=Color.black;
        }
        LogLevel._logLevelColorMap.put(level,color);
    }
    public static void resetLogLevelColorMap(){
        LogLevel._logLevelColorMap.clear();
        for(int i=0;i<LogLevel._allDefaultLevels.length;++i){
            LogLevel._logLevelColorMap.put(LogLevel._allDefaultLevels[i],Color.black);
        }
    }
    public static List getLog4JLevels(){
        return Arrays.asList(LogLevel._log4JLevels);
    }
    public static List getJdk14Levels(){
        return Arrays.asList(LogLevel._jdk14Levels);
    }
    public static List getAllDefaultLevels(){
        return Arrays.asList(LogLevel._allDefaultLevels);
    }
    public static Map getLogLevelColorMap(){
        return LogLevel._logLevelColorMap;
    }
    protected int getPrecedence(){
        return this._precedence;
    }
    static{
        FATAL=new LogLevel("FATAL",0);
        ERROR=new LogLevel("ERROR",1);
        WARN=new LogLevel("WARN",2);
        INFO=new LogLevel("INFO",3);
        DEBUG=new LogLevel("DEBUG",4);
        SEVERE=new LogLevel("SEVERE",1);
        WARNING=new LogLevel("WARNING",2);
        CONFIG=new LogLevel("CONFIG",4);
        FINE=new LogLevel("FINE",5);
        FINER=new LogLevel("FINER",6);
        FINEST=new LogLevel("FINEST",7);
        LogLevel._registeredLogLevelMap=new HashMap();
        LogLevel._log4JLevels=new LogLevel[] { LogLevel.FATAL,LogLevel.ERROR,LogLevel.WARN,LogLevel.INFO,LogLevel.DEBUG };
        LogLevel._jdk14Levels=new LogLevel[] { LogLevel.SEVERE,LogLevel.WARNING,LogLevel.INFO,LogLevel.CONFIG,LogLevel.FINE,LogLevel.FINER,LogLevel.FINEST };
        LogLevel._allDefaultLevels=new LogLevel[] { LogLevel.FATAL,LogLevel.ERROR,LogLevel.WARN,LogLevel.INFO,LogLevel.DEBUG,LogLevel.SEVERE,LogLevel.WARNING,LogLevel.CONFIG,LogLevel.FINE,LogLevel.FINER,LogLevel.FINEST };
        LogLevel._logLevelMap=new HashMap();
        for(int i=0;i<LogLevel._allDefaultLevels.length;++i){
            LogLevel._logLevelMap.put(LogLevel._allDefaultLevels[i].getLabel(),LogLevel._allDefaultLevels[i]);
        }
        LogLevel._logLevelColorMap=new HashMap();
        for(int j=0;j<LogLevel._allDefaultLevels.length;++j){
            LogLevel._logLevelColorMap.put(LogLevel._allDefaultLevels[j],Color.black);
        }
    }
}
