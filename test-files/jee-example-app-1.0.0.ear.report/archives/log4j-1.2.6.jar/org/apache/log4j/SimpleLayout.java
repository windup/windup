package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Layout;

public class SimpleLayout extends Layout{
    StringBuffer sbuf;
    public SimpleLayout(){
        super();
        this.sbuf=new StringBuffer(128);
    }
    public void activateOptions(){
    }
    public String format(final LoggingEvent event){
        this.sbuf.setLength(0);
        this.sbuf.append(event.getLevel().toString());
        this.sbuf.append(" - ");
        this.sbuf.append(event.getRenderedMessage());
        this.sbuf.append(Layout.LINE_SEP);
        return this.sbuf.toString();
    }
    public boolean ignoresThrowable(){
        return true;
    }
}
