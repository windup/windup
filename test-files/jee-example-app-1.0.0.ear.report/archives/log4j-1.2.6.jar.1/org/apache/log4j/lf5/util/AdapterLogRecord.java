package org.apache.log4j.lf5.util;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;

public class AdapterLogRecord extends LogRecord{
    private static LogLevel severeLevel;
    private static StringWriter sw;
    private static PrintWriter pw;
    public void setCategory(final String category){
        super.setCategory(category);
        super.setLocation(this.getLocationInfo(category));
    }
    public boolean isSevereLevel(){
        return AdapterLogRecord.severeLevel!=null&&AdapterLogRecord.severeLevel.equals(this.getLevel());
    }
    public static void setSevereLevel(final LogLevel level){
        AdapterLogRecord.severeLevel=level;
    }
    public static LogLevel getSevereLevel(){
        return AdapterLogRecord.severeLevel;
    }
    protected String getLocationInfo(final String category){
        final String stackTrace=this.stackTraceToString(new Throwable());
        final String line=this.parseLine(stackTrace,category);
        return line;
    }
    protected String stackTraceToString(final Throwable t){
        String s=null;
        synchronized(AdapterLogRecord.sw){
            t.printStackTrace(AdapterLogRecord.pw);
            s=AdapterLogRecord.sw.toString();
            AdapterLogRecord.sw.getBuffer().setLength(0);
        }
        return s;
    }
    protected String parseLine(String trace,final String category){
        final int index=trace.indexOf(category);
        if(index==-1){
            return null;
        }
        trace=trace.substring(index);
        trace=trace.substring(0,trace.indexOf(")")+1);
        return trace;
    }
    static{
        AdapterLogRecord.severeLevel=null;
        AdapterLogRecord.sw=new StringWriter();
        AdapterLogRecord.pw=new PrintWriter(AdapterLogRecord.sw);
    }
}
