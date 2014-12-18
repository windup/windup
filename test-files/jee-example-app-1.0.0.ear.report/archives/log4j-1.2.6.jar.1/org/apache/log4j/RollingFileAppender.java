package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import java.io.Writer;
import org.apache.log4j.helpers.OptionConverter;
import java.io.File;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.CountingQuietWriter;
import java.io.IOException;
import org.apache.log4j.Layout;
import org.apache.log4j.FileAppender;

public class RollingFileAppender extends FileAppender{
    protected long maxFileSize;
    protected int maxBackupIndex;
    public RollingFileAppender(){
        super();
        this.maxFileSize=10485760L;
        this.maxBackupIndex=1;
    }
    public RollingFileAppender(final Layout layout,final String filename,final boolean append) throws IOException{
        super(layout,filename,append);
        this.maxFileSize=10485760L;
        this.maxBackupIndex=1;
    }
    public RollingFileAppender(final Layout layout,final String filename) throws IOException{
        super(layout,filename);
        this.maxFileSize=10485760L;
        this.maxBackupIndex=1;
    }
    public int getMaxBackupIndex(){
        return this.maxBackupIndex;
    }
    public long getMaximumFileSize(){
        return this.maxFileSize;
    }
    public void rollOver(){
        LogLog.debug("rolling over count="+((CountingQuietWriter)super.qw).getCount());
        LogLog.debug("maxBackupIndex="+this.maxBackupIndex);
        if(this.maxBackupIndex>0){
            File file=new File(super.fileName+'.'+this.maxBackupIndex);
            if(file.exists()){
                file.delete();
            }
            for(int i=this.maxBackupIndex-1;i>=1;--i){
                file=new File(super.fileName+"."+i);
                if(file.exists()){
                    final File target=new File(super.fileName+'.'+(i+1));
                    LogLog.debug("Renaming file "+file+" to "+target);
                    file.renameTo(target);
                }
            }
            final File target=new File(super.fileName+"."+1);
            this.closeFile();
            file=new File(super.fileName);
            LogLog.debug("Renaming file "+file+" to "+target);
            file.renameTo(target);
        }
        try{
            this.setFile(super.fileName,false,super.bufferedIO,super.bufferSize);
        }
        catch(IOException e){
            LogLog.error("setFile("+super.fileName+", false) call failed.",e);
        }
    }
    public synchronized void setFile(final String fileName,final boolean append,final boolean bufferedIO,final int bufferSize) throws IOException{
        super.setFile(fileName,append,super.bufferedIO,super.bufferSize);
        if(append){
            final File f=new File(fileName);
            ((CountingQuietWriter)super.qw).setCount(f.length());
        }
    }
    public void setMaxBackupIndex(final int maxBackups){
        this.maxBackupIndex=maxBackups;
    }
    public void setMaximumFileSize(final long maxFileSize){
        this.maxFileSize=maxFileSize;
    }
    public void setMaxFileSize(final String value){
        this.maxFileSize=OptionConverter.toFileSize(value,this.maxFileSize+1L);
    }
    protected void setQWForFiles(final Writer writer){
        super.qw=new CountingQuietWriter(writer,super.errorHandler);
    }
    protected void subAppend(final LoggingEvent event){
        super.subAppend(event);
        if(super.fileName!=null&&((CountingQuietWriter)super.qw).getCount()>=this.maxFileSize){
            this.rollOver();
        }
    }
}
