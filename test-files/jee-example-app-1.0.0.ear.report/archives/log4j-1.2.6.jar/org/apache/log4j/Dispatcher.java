package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.helpers.BoundedFIFO;

class Dispatcher extends Thread{
    BoundedFIFO bf;
    AppenderAttachableImpl aai;
    boolean interrupted;
    AsyncAppender container;
    Dispatcher(final BoundedFIFO bf,final AsyncAppender container){
        super();
        this.interrupted=false;
        this.bf=bf;
        this.container=container;
        this.aai=container.aai;
        this.setDaemon(true);
        this.setPriority(1);
        this.setName("Dispatcher-"+this.getName());
    }
    void close(){
        synchronized(this.bf){
            this.interrupted=true;
            if(this.bf.length()==0){
                this.bf.notify();
            }
        }
    }
    public void run(){
        while(true){
            final LoggingEvent event;
            synchronized(this.bf){
                if(this.bf.length()==0){
                    if(this.interrupted){
                        break;
                    }
                    try{
                        this.bf.wait();
                    }
                    catch(InterruptedException e){
                        LogLog.error("The dispathcer should not be interrupted.");
                        break;
                    }
                }
                event=this.bf.get();
                if(this.bf.wasFull()){
                    this.bf.notify();
                }
            }
            synchronized(this.container.aai){
                if(this.aai==null||event==null){
                    continue;
                }
                this.aai.appendLoopOnAppenders(event);
                continue;
            }
            break;
        }
        this.aai.removeAllAppenders();
    }
}
