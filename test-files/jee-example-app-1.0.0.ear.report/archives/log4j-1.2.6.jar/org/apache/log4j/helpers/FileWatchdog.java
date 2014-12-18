package org.apache.log4j.helpers;

import org.apache.log4j.helpers.LogLog;
import java.io.File;

public abstract class FileWatchdog extends Thread{
    public static final long DEFAULT_DELAY=60000L;
    protected String filename;
    protected long delay;
    File file;
    long lastModif;
    boolean warnedAlready;
    boolean interrupted;
    protected FileWatchdog(final String filename){
        super();
        this.delay=60000L;
        this.lastModif=0L;
        this.warnedAlready=false;
        this.interrupted=false;
        this.filename=filename;
        this.file=new File(filename);
        this.setDaemon(true);
        this.checkAndConfigure();
    }
    public void setDelay(final long delay){
        this.delay=delay;
    }
    protected abstract void doOnChange();
    protected void checkAndConfigure(){
        boolean fileExists;
        try{
            fileExists=this.file.exists();
        }
        catch(SecurityException e){
            LogLog.warn("Was not allowed to read check file existance, file:["+this.filename+"].");
            this.interrupted=true;
            return;
        }
        if(fileExists){
            final long l=this.file.lastModified();
            if(l>this.lastModif){
                this.lastModif=l;
                this.doOnChange();
                this.warnedAlready=false;
            }
        }
        else if(!this.warnedAlready){
            LogLog.debug("["+this.filename+"] does not exist.");
            this.warnedAlready=true;
        }
    }
    public void run(){
        while(!this.interrupted){
            try{
                Thread.currentThread();
                Thread.sleep(this.delay);
            }
            catch(InterruptedException ex){
            }
            this.checkAndConfigure();
        }
    }
}
