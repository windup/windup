package org.apache.log4j.varia;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.AppenderSkeleton;

public class NullAppender extends AppenderSkeleton{
    private static NullAppender instance;
    public void activateOptions(){
    }
    public NullAppender getInstance(){
        return NullAppender.instance;
    }
    public void close(){
    }
    public void doAppend(final LoggingEvent event){
    }
    protected void append(final LoggingEvent event){
    }
    public boolean requiresLayout(){
        return false;
    }
    static{
        NullAppender.instance=new NullAppender();
    }
}
