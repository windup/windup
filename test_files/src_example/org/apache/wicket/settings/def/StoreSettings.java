package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.*;
import java.io.*;

public class StoreSettings implements IStoreSettings{
    private static final int DEFAULT_CACHE_SIZE=40;
    private static final Bytes DEFAULT_MAX_SIZE_PER_SESSION;
    private static final int DEFAULT_ASYNCHRONOUS_QUEUE_CAPACITY=100;
    private int inmemoryCacheSize;
    private Bytes maxSizePerSession;
    private File fileStoreFolder;
    private int asynchronousQueueCapacity;
    private boolean isAsynchronous;
    public StoreSettings(final Application application){
        super();
        this.inmemoryCacheSize=40;
        this.maxSizePerSession=StoreSettings.DEFAULT_MAX_SIZE_PER_SESSION;
        this.fileStoreFolder=null;
        this.asynchronousQueueCapacity=100;
        this.isAsynchronous=true;
    }
    public int getInmemoryCacheSize(){
        return this.inmemoryCacheSize;
    }
    public void setInmemoryCacheSize(final int inmemoryCacheSize){
        this.inmemoryCacheSize=inmemoryCacheSize;
    }
    public Bytes getMaxSizePerSession(){
        return this.maxSizePerSession;
    }
    public void setMaxSizePerSession(final Bytes maxSizePerSession){
        this.maxSizePerSession=(Bytes)Args.notNull((Object)maxSizePerSession,"maxSizePerSession");
    }
    public File getFileStoreFolder(){
        if(this.fileStoreFolder==null){
            if(Application.exists()){
                this.fileStoreFolder=(File)((WebApplication)Application.get()).getServletContext().getAttribute("javax.servlet.context.tempdir");
            }
            if(this.fileStoreFolder!=null){
                return this.fileStoreFolder;
            }
            try{
                this.fileStoreFolder=File.createTempFile("file-prefix",null).getParentFile();
            }
            catch(IOException e){
                throw new WicketRuntimeException(e);
            }
        }
        return this.fileStoreFolder;
    }
    public void setFileStoreFolder(final File fileStoreFolder){
        this.fileStoreFolder=(File)Args.notNull((Object)fileStoreFolder,"fileStoreFolder");
    }
    public int getAsynchronousQueueCapacity(){
        return this.asynchronousQueueCapacity;
    }
    public void setAsynchronousQueueCapacity(final int queueCapacity){
        if(queueCapacity<1){
            throw new IllegalArgumentException("The capacity of the asynchronous queue should be at least 1.");
        }
        this.asynchronousQueueCapacity=queueCapacity;
    }
    public void setAsynchronous(final boolean async){
        this.isAsynchronous=async;
    }
    public boolean isAsynchronous(){
        return this.isAsynchronous;
    }
    static{
        DEFAULT_MAX_SIZE_PER_SESSION=Bytes.megabytes(10L);
    }
}
