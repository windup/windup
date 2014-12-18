package org.apache.log4j;

import java.util.Enumeration;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Appender;
import org.apache.log4j.Dispatcher;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.helpers.BoundedFIFO;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.AppenderSkeleton;

public class AsyncAppender extends AppenderSkeleton implements AppenderAttachable{
    public static final int DEFAULT_BUFFER_SIZE=128;
    BoundedFIFO bf;
    AppenderAttachableImpl aai;
    Dispatcher dispatcher;
    boolean locationInfo;
    boolean interruptedWarningMessage;
    public AsyncAppender(){
        super();
        this.bf=new BoundedFIFO(128);
        this.locationInfo=false;
        this.interruptedWarningMessage=false;
        this.aai=new AppenderAttachableImpl();
        (this.dispatcher=new Dispatcher(this.bf,this)).start();
    }
    public void addAppender(final Appender newAppender){
        synchronized(this.aai){
            this.aai.addAppender(newAppender);
        }
    }
    public void append(final LoggingEvent event){
        event.getNDC();
        event.getThreadName();
        event.getMDCCopy();
        if(this.locationInfo){
            event.getLocationInformation();
        }
        synchronized(this.bf){
            while(this.bf.isFull()){
                try{
                    this.bf.wait();
                }
                catch(InterruptedException e){
                    if(!this.interruptedWarningMessage){
                        this.interruptedWarningMessage=true;
                        LogLog.warn("AsyncAppender interrupted.",e);
                    }
                    else{
                        LogLog.warn("AsyncAppender interrupted again.");
                    }
                }
            }
            this.bf.put(event);
            if(this.bf.wasEmpty()){
                this.bf.notify();
            }
        }
    }
    public void close(){
        synchronized(this){
            if(super.closed){
                return;
            }
            super.closed=true;
        }
        this.dispatcher.close();
        try{
            this.dispatcher.join();
        }
        catch(InterruptedException e){
            LogLog.error("Got an InterruptedException while waiting for the dispatcher to finish.",e);
        }
        this.dispatcher=null;
        this.bf=null;
    }
    public Enumeration getAllAppenders(){
        synchronized(this.aai){
            return this.aai.getAllAppenders();
        }
    }
    public Appender getAppender(final String name){
        synchronized(this.aai){
            return this.aai.getAppender(name);
        }
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    public boolean isAttached(final Appender appender){
        return this.aai.isAttached(appender);
    }
    public boolean requiresLayout(){
        return false;
    }
    public void removeAllAppenders(){
        synchronized(this.aai){
            this.aai.removeAllAppenders();
        }
    }
    public void removeAppender(final Appender appender){
        synchronized(this.aai){
            this.aai.removeAppender(appender);
        }
    }
    public void removeAppender(final String name){
        synchronized(this.aai){
            this.aai.removeAppender(name);
        }
    }
    public void setLocationInfo(final boolean flag){
        this.locationInfo=flag;
    }
    public void setBufferSize(final int size){
        this.bf.resize(size);
    }
    public int getBufferSize(){
        return this.bf.getMaxSize();
    }
}
