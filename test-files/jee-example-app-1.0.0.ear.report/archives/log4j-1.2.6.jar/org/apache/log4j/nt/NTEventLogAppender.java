package org.apache.log4j.nt;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.AppenderSkeleton;

public class NTEventLogAppender extends AppenderSkeleton{
    private int _handle;
    private String source;
    private String server;
    private static final int FATAL;
    private static final int ERROR;
    private static final int WARN;
    private static final int INFO;
    private static final int DEBUG;
    public NTEventLogAppender(){
        this(null,null,null);
    }
    public NTEventLogAppender(final String source){
        this(null,source,null);
    }
    public NTEventLogAppender(final String server,final String source){
        this(server,source,null);
    }
    public NTEventLogAppender(final Layout layout){
        this(null,null,layout);
    }
    public NTEventLogAppender(final String source,final Layout layout){
        this(null,source,layout);
    }
    public NTEventLogAppender(final String server,String source,final Layout layout){
        super();
        this._handle=0;
        this.source=null;
        this.server=null;
        if(source==null){
            source="Log4j";
        }
        if(layout==null){
            super.layout=new TTCCLayout();
        }
        else{
            super.layout=layout;
        }
        try{
            this._handle=this.registerEventSource(server,source);
        }
        catch(Exception e){
            e.printStackTrace();
            this._handle=0;
        }
    }
    public void close(){
    }
    public void activateOptions(){
        if(this.source!=null){
            try{
                this._handle=this.registerEventSource(this.server,this.source);
            }
            catch(Exception e){
                LogLog.error("Could not register event source.",e);
                this._handle=0;
            }
        }
    }
    public void append(final LoggingEvent event){
        final StringBuffer sbuf=new StringBuffer();
        sbuf.append(super.layout.format(event));
        if(super.layout.ignoresThrowable()){
            final String[] s=event.getThrowableStrRep();
            if(s!=null){
                for(int len=s.length,i=0;i<len;++i){
                    sbuf.append(s[i]);
                }
            }
        }
        final int nt_category=event.getLevel().toInt();
        this.reportEvent(this._handle,sbuf.toString(),nt_category);
    }
    public void finalize(){
        this.deregisterEventSource(this._handle);
        this._handle=0;
    }
    public void setSource(final String source){
        this.source=source.trim();
    }
    public String getSource(){
        return this.source;
    }
    public boolean requiresLayout(){
        return true;
    }
    private native int registerEventSource(final String p0,final String p1);
    private native void reportEvent(final int p0,final String p1,final int p2);
    private native void deregisterEventSource(final int p0);
    static{
        FATAL=Level.FATAL.toInt();
        ERROR=Level.ERROR.toInt();
        WARN=Level.WARN.toInt();
        INFO=Level.INFO.toInt();
        DEBUG=Level.DEBUG.toInt();
        System.loadLibrary("NTEventLogAppender");
    }
}
