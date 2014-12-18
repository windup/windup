package org.apache.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import java.util.TimeZone;
import org.apache.log4j.helpers.DateLayout;

public class TTCCLayout extends DateLayout{
    private boolean threadPrinting;
    private boolean categoryPrefixing;
    private boolean contextPrinting;
    protected final StringBuffer buf;
    public TTCCLayout(){
        super();
        this.threadPrinting=true;
        this.categoryPrefixing=true;
        this.contextPrinting=true;
        this.buf=new StringBuffer(256);
        this.setDateFormat("RELATIVE",null);
    }
    public TTCCLayout(final String dateFormatType){
        super();
        this.threadPrinting=true;
        this.categoryPrefixing=true;
        this.contextPrinting=true;
        this.buf=new StringBuffer(256);
        this.setDateFormat(dateFormatType);
    }
    public void setThreadPrinting(final boolean threadPrinting){
        this.threadPrinting=threadPrinting;
    }
    public boolean getThreadPrinting(){
        return this.threadPrinting;
    }
    public void setCategoryPrefixing(final boolean categoryPrefixing){
        this.categoryPrefixing=categoryPrefixing;
    }
    public boolean getCategoryPrefixing(){
        return this.categoryPrefixing;
    }
    public void setContextPrinting(final boolean contextPrinting){
        this.contextPrinting=contextPrinting;
    }
    public boolean getContextPrinting(){
        return this.contextPrinting;
    }
    public String format(final LoggingEvent event){
        this.buf.setLength(0);
        this.dateFormat(this.buf,event);
        if(this.threadPrinting){
            this.buf.append('[');
            this.buf.append(event.getThreadName());
            this.buf.append("] ");
        }
        this.buf.append(event.getLevel().toString());
        this.buf.append(' ');
        if(this.categoryPrefixing){
            this.buf.append(event.getLoggerName());
            this.buf.append(' ');
        }
        if(this.contextPrinting){
            final String ndc=event.getNDC();
            if(ndc!=null){
                this.buf.append(ndc);
                this.buf.append(' ');
            }
        }
        this.buf.append("- ");
        this.buf.append(event.getRenderedMessage());
        this.buf.append(Layout.LINE_SEP);
        return this.buf.toString();
    }
    public boolean ignoresThrowable(){
        return true;
    }
}
