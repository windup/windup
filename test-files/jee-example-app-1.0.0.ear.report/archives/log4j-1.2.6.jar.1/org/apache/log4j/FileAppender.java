package org.apache.log4j;

import org.apache.log4j.helpers.QuietWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.log4j.helpers.LogLog;
import java.io.IOException;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

public class FileAppender extends WriterAppender{
    protected boolean fileAppend;
    protected String fileName;
    protected boolean bufferedIO;
    protected int bufferSize;
    public FileAppender(){
        super();
        this.fileAppend=true;
        this.fileName=null;
        this.bufferedIO=false;
        this.bufferSize=8192;
    }
    public FileAppender(final Layout layout,final String filename,final boolean append,final boolean bufferedIO,final int bufferSize) throws IOException{
        super();
        this.fileAppend=true;
        this.fileName=null;
        this.bufferedIO=false;
        this.bufferSize=8192;
        super.layout=layout;
        this.setFile(filename,append,bufferedIO,bufferSize);
    }
    public FileAppender(final Layout layout,final String filename,final boolean append) throws IOException{
        super();
        this.fileAppend=true;
        this.fileName=null;
        this.bufferedIO=false;
        this.bufferSize=8192;
        super.layout=layout;
        this.setFile(filename,append,false,this.bufferSize);
    }
    public FileAppender(final Layout layout,final String filename) throws IOException{
        this(layout,filename,true);
    }
    public void setFile(final String file){
        final String val=file.trim();
        this.fileName=val;
    }
    public boolean getAppend(){
        return this.fileAppend;
    }
    public String getFile(){
        return this.fileName;
    }
    public void activateOptions(){
        if(this.fileName!=null){
            try{
                this.setFile(this.fileName,this.fileAppend,this.bufferedIO,this.bufferSize);
            }
            catch(IOException e){
                super.errorHandler.error("setFile("+this.fileName+","+this.fileAppend+") call failed.",e,4);
            }
        }
        else{
            LogLog.warn("File option not set for appender ["+super.name+"].");
            LogLog.warn("Are you using FileAppender instead of ConsoleAppender?");
        }
    }
    protected void closeFile(){
        if(super.qw!=null){
            try{
                super.qw.close();
            }
            catch(IOException e){
                LogLog.error("Could not close "+super.qw,e);
            }
        }
    }
    public boolean getBufferedIO(){
        return this.bufferedIO;
    }
    public int getBufferSize(){
        return this.bufferSize;
    }
    public void setAppend(final boolean flag){
        this.fileAppend=flag;
    }
    public void setBufferedIO(final boolean bufferedIO){
        this.bufferedIO=bufferedIO;
        if(bufferedIO){
            super.immediateFlush=false;
        }
    }
    public void setBufferSize(final int bufferSize){
        this.bufferSize=bufferSize;
    }
    public synchronized void setFile(final String fileName,final boolean append,final boolean bufferedIO,final int bufferSize) throws IOException{
        LogLog.debug("setFile called: "+fileName+", "+append);
        if(bufferedIO){
            this.setImmediateFlush(false);
        }
        this.reset();
        Writer fw=this.createWriter(new FileOutputStream(fileName,append));
        if(bufferedIO){
            fw=new BufferedWriter(fw,bufferSize);
        }
        this.setQWForFiles(fw);
        this.fileName=fileName;
        this.fileAppend=append;
        this.bufferedIO=bufferedIO;
        this.bufferSize=bufferSize;
        this.writeHeader();
        LogLog.debug("setFile ended");
    }
    protected void setQWForFiles(final Writer writer){
        super.qw=new QuietWriter(writer,super.errorHandler);
    }
    protected void reset(){
        this.closeFile();
        this.fileName=null;
        super.reset();
    }
}
