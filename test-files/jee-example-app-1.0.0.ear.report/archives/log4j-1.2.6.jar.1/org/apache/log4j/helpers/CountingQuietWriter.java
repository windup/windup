package org.apache.log4j.helpers;

import java.io.IOException;
import org.apache.log4j.spi.ErrorHandler;
import java.io.Writer;
import org.apache.log4j.helpers.QuietWriter;

public class CountingQuietWriter extends QuietWriter{
    protected long count;
    public CountingQuietWriter(final Writer writer,final ErrorHandler eh){
        super(writer,eh);
    }
    public void write(final String string){
        try{
            super.out.write(string);
            this.count+=string.length();
        }
        catch(IOException e){
            super.errorHandler.error("Write failure.",e,1);
        }
    }
    public long getCount(){
        return this.count;
    }
    public void setCount(final long count){
        this.count=count;
    }
}
