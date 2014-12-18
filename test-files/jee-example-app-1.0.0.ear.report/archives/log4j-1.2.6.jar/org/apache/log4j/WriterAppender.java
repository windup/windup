package org.apache.log4j;

import org.apache.log4j.spi.ErrorHandler;
import java.io.IOException;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.AppenderSkeleton;

public class WriterAppender extends AppenderSkeleton{
    protected boolean immediateFlush;
    protected String encoding;
    protected QuietWriter qw;
    public WriterAppender(){
        super();
        this.immediateFlush=true;
    }
    public WriterAppender(final Layout layout,final OutputStream os){
        this(layout,new OutputStreamWriter(os));
    }
    public WriterAppender(final Layout layout,final Writer writer){
        super();
        this.immediateFlush=true;
        super.layout=layout;
        this.setWriter(writer);
    }
    public void setImmediateFlush(final boolean value){
        this.immediateFlush=value;
    }
    public boolean getImmediateFlush(){
        return this.immediateFlush;
    }
    public void activateOptions(){
    }
    public void append(final LoggingEvent event){
        if(!this.checkEntryConditions()){
            return;
        }
        this.subAppend(event);
    }
    protected boolean checkEntryConditions(){
        if(super.closed){
            LogLog.warn("Not allowed to write to a closed appender.");
            return false;
        }
        if(this.qw==null){
            super.errorHandler.error("No output stream or file set for the appender named ["+super.name+"].");
            return false;
        }
        if(super.layout==null){
            super.errorHandler.error("No layout set for the appender named ["+super.name+"].");
            return false;
        }
        return true;
    }
    public synchronized void close(){
        if(super.closed){
            return;
        }
        super.closed=true;
        this.writeFooter();
        this.reset();
    }
    protected void closeWriter(){
        if(this.qw!=null){
            try{
                this.qw.close();
            }
            catch(IOException e){
                LogLog.error("Could not close "+this.qw,e);
            }
        }
    }
    protected OutputStreamWriter createWriter(final OutputStream os){
        OutputStreamWriter retval=null;
        final String enc=this.getEncoding();
        if(enc!=null){
            try{
                retval=new OutputStreamWriter(os,enc);
            }
            catch(IOException e){
                LogLog.warn("Error initializing output writer.");
                LogLog.warn("Unsupported encoding?");
            }
        }
        if(retval==null){
            retval=new OutputStreamWriter(os);
        }
        return retval;
    }
    public String getEncoding(){
        return this.encoding;
    }
    public void setEncoding(final String value){
        this.encoding=value;
    }
    public synchronized void setErrorHandler(final ErrorHandler eh){
        if(eh==null){
            LogLog.warn("You have tried to set a null error-handler.");
        }
        else{
            super.errorHandler=eh;
            if(this.qw!=null){
                this.qw.setErrorHandler(eh);
            }
        }
    }
    public synchronized void setWriter(final Writer writer){
        this.reset();
        this.qw=new QuietWriter(writer,super.errorHandler);
        this.writeHeader();
    }
    protected void subAppend(final LoggingEvent event){
        this.qw.write(super.layout.format(event));
        if(super.layout.ignoresThrowable()){
            final String[] s=event.getThrowableStrRep();
            if(s!=null){
                for(int len=s.length,i=0;i<len;++i){
                    this.qw.write(s[i]);
                    this.qw.write(Layout.LINE_SEP);
                }
            }
        }
        if(this.immediateFlush){
            this.qw.flush();
        }
    }
    public boolean requiresLayout(){
        return true;
    }
    protected void reset(){
        this.closeWriter();
        this.qw=null;
    }
    protected void writeFooter(){
        if(super.layout!=null){
            final String f=super.layout.getFooter();
            if(f!=null&&this.qw!=null){
                this.qw.write(f);
                this.qw.flush();
            }
        }
    }
    protected void writeHeader(){
        if(super.layout!=null){
            final String h=super.layout.getHeader();
            if(h!=null&&this.qw!=null){
                this.qw.write(h);
            }
        }
    }
}
