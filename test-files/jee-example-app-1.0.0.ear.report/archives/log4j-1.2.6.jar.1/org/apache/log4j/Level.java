package org.apache.log4j;

import org.apache.log4j.Priority;

public class Level extends Priority{
    public static final Level OFF;
    public static final Level FATAL;
    public static final Level ERROR;
    public static final Level WARN;
    public static final Level INFO;
    public static final Level DEBUG;
    public static final Level ALL;
    protected Level(final int level,final String levelStr,final int syslogEquivalent){
        super(level,levelStr,syslogEquivalent);
    }
    public static Level toLevel(final String sArg){
        return toLevel(sArg,Level.DEBUG);
    }
    public static Level toLevel(final int val){
        return toLevel(val,Level.DEBUG);
    }
    public static Level toLevel(final int val,final Level defaultLevel){
        switch(val){
            case Integer.MIN_VALUE:{
                return Level.ALL;
            }
            case 10000:{
                return Level.DEBUG;
            }
            case 20000:{
                return Level.INFO;
            }
            case 30000:{
                return Level.WARN;
            }
            case 40000:{
                return Level.ERROR;
            }
            case 50000:{
                return Level.FATAL;
            }
            case Integer.MAX_VALUE:{
                return Level.OFF;
            }
            default:{
                return defaultLevel;
            }
        }
    }
    public static Level toLevel(final String sArg,final Level defaultLevel){
        if(sArg==null){
            return defaultLevel;
        }
        final String s=sArg.toUpperCase();
        if(s.equals("ALL")){
            return Level.ALL;
        }
        if(s.equals("DEBUG")){
            return Level.DEBUG;
        }
        if(s.equals("INFO")){
            return Level.INFO;
        }
        if(s.equals("WARN")){
            return Level.WARN;
        }
        if(s.equals("ERROR")){
            return Level.ERROR;
        }
        if(s.equals("FATAL")){
            return Level.FATAL;
        }
        if(s.equals("OFF")){
            return Level.OFF;
        }
        return defaultLevel;
    }
    static{
        OFF=new Level(Integer.MAX_VALUE,"OFF",0);
        FATAL=new Level(50000,"FATAL",0);
        ERROR=new Level(40000,"ERROR",3);
        WARN=new Level(30000,"WARN",4);
        INFO=new Level(20000,"INFO",6);
        DEBUG=new Level(10000,"DEBUG",7);
        ALL=new Level(Integer.MIN_VALUE,"ALL",7);
    }
}
