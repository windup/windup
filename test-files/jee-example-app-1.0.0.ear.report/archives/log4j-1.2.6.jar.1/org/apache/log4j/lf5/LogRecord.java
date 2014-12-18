package org.apache.log4j.lf5;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.log4j.lf5.LogLevel;
import java.io.Serializable;

public abstract class LogRecord implements Serializable{
    protected static long _seqCount;
    protected LogLevel _level;
    protected String _message;
    protected long _sequenceNumber;
    protected long _millis;
    protected String _category;
    protected String _thread;
    protected String _thrownStackTrace;
    protected Throwable _thrown;
    protected String _ndc;
    protected String _location;
    public LogRecord(){
        super();
        this._millis=System.currentTimeMillis();
        this._category="Debug";
        this._message="";
        this._level=LogLevel.INFO;
        this._sequenceNumber=getNextId();
        this._thread=Thread.currentThread().toString();
        this._ndc="";
        this._location="";
    }
    public LogLevel getLevel(){
        return this._level;
    }
    public void setLevel(final LogLevel level){
        this._level=level;
    }
    public abstract boolean isSevereLevel();
    public boolean hasThrown(){
        final Throwable thrown=this.getThrown();
        if(thrown==null){
            return false;
        }
        final String thrownString=thrown.toString();
        return thrownString!=null&&thrownString.trim().length()!=0;
    }
    public boolean isFatal(){
        return this.isSevereLevel()||this.hasThrown();
    }
    public String getCategory(){
        return this._category;
    }
    public void setCategory(final String category){
        this._category=category;
    }
    public String getMessage(){
        return this._message;
    }
    public void setMessage(final String message){
        this._message=message;
    }
    public long getSequenceNumber(){
        return this._sequenceNumber;
    }
    public void setSequenceNumber(final long number){
        this._sequenceNumber=number;
    }
    public long getMillis(){
        return this._millis;
    }
    public void setMillis(final long millis){
        this._millis=millis;
    }
    public String getThreadDescription(){
        return this._thread;
    }
    public void setThreadDescription(final String threadDescription){
        this._thread=threadDescription;
    }
    public String getThrownStackTrace(){
        return this._thrownStackTrace;
    }
    public void setThrownStackTrace(final String trace){
        this._thrownStackTrace=trace;
    }
    public Throwable getThrown(){
        return this._thrown;
    }
    public void setThrown(final Throwable thrown){
        if(thrown==null){
            return;
        }
        this._thrown=thrown;
        StringWriter sw=new StringWriter();
        PrintWriter out=new PrintWriter(sw);
        thrown.printStackTrace(out);
        out.flush();
        this._thrownStackTrace=sw.toString();
        try{
            out.close();
            sw.close();
        }
        catch(IOException ex){
        }
        out=null;
        sw=null;
    }
    public String toString(){
        final StringBuffer buf=new StringBuffer();
        buf.append("LogRecord: ["+this._level+", "+this._message+"]");
        return buf.toString();
    }
    public String getNDC(){
        return this._ndc;
    }
    public void setNDC(final String ndc){
        this._ndc=ndc;
    }
    public String getLocation(){
        return this._location;
    }
    public void setLocation(final String location){
        this._location=location;
    }
    public static synchronized void resetSequenceNumber(){
        LogRecord._seqCount=0L;
    }
    protected static synchronized long getNextId(){
        return ++LogRecord._seqCount;
    }
    static{
        LogRecord._seqCount=0L;
    }
}
