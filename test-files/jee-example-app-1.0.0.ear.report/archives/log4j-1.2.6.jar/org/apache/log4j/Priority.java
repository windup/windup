package org.apache.log4j;

import org.apache.log4j.Level;

public class Priority{
    int level;
    String levelStr;
    int syslogEquivalent;
    public static final int OFF_INT=Integer.MAX_VALUE;
    public static final int FATAL_INT=50000;
    public static final int ERROR_INT=40000;
    public static final int WARN_INT=30000;
    public static final int INFO_INT=20000;
    public static final int DEBUG_INT=10000;
    public static final int ALL_INT=Integer.MIN_VALUE;
    public static final Priority FATAL;
    public static final Priority ERROR;
    public static final Priority WARN;
    public static final Priority INFO;
    public static final Priority DEBUG;
    protected Priority(final int level,final String levelStr,final int syslogEquivalent){
        super();
        this.level=level;
        this.levelStr=levelStr;
        this.syslogEquivalent=syslogEquivalent;
    }
    public boolean equals(final Object o){
        if(o instanceof Priority){
            final Priority r=(Priority)o;
            return this.level==r.level;
        }
        return false;
    }
    public final int getSyslogEquivalent(){
        return this.syslogEquivalent;
    }
    public boolean isGreaterOrEqual(final Priority r){
        return this.level>=r.level;
    }
    public static Priority[] getAllPossiblePriorities(){
        return new Priority[] { Priority.FATAL,Priority.ERROR,Level.WARN,Priority.INFO,Priority.DEBUG };
    }
    public final String toString(){
        return this.levelStr;
    }
    public final int toInt(){
        return this.level;
    }
    public static Priority toPriority(final String sArg){
        return Level.toLevel(sArg);
    }
    public static Priority toPriority(final int val){
        return toPriority(val,Priority.DEBUG);
    }
    public static Priority toPriority(final int val,final Priority defaultPriority){
        return Level.toLevel(val,(Level)defaultPriority);
    }
    public static Priority toPriority(final String sArg,final Priority defaultPriority){
        return Level.toLevel(sArg,(Level)defaultPriority);
    }
    static{
        FATAL=new Level(50000,"FATAL",0);
        ERROR=new Level(40000,"ERROR",3);
        WARN=new Level(30000,"WARN",4);
        INFO=new Level(20000,"INFO",6);
        DEBUG=new Level(10000,"DEBUG",7);
    }
}
