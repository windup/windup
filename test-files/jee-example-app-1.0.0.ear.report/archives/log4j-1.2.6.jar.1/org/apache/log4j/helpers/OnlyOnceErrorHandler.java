package org.apache.log4j.helpers;

import org.apache.log4j.Appender;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;

public class OnlyOnceErrorHandler implements ErrorHandler{
    final String WARN_PREFIX="log4j warning: ";
    final String ERROR_PREFIX="log4j error: ";
    boolean firstTime;
    public OnlyOnceErrorHandler(){
        super();
        this.firstTime=true;
    }
    public void setLogger(final Logger logger){
    }
    public void activateOptions(){
    }
    public void error(final String message,final Exception e,final int errorCode){
        this.error(message,e,errorCode,null);
    }
    public void error(final String message,final Exception e,final int errorCode,final LoggingEvent event){
        if(this.firstTime){
            LogLog.error(message,e);
            this.firstTime=false;
        }
    }
    public void error(final String message){
        if(this.firstTime){
            LogLog.error(message);
            this.firstTime=false;
        }
    }
    public void setAppender(final Appender appender){
    }
    public void setBackupAppender(final Appender appender){
    }
}
