package org.apache.log4j.helpers;

import java.io.IOException;
import java.io.Writer;
import org.apache.log4j.spi.ErrorHandler;
import java.io.FilterWriter;

public class QuietWriter extends FilterWriter{
    protected ErrorHandler errorHandler;
    public QuietWriter(final Writer writer,final ErrorHandler errorHandler){
        super(writer);
        this.setErrorHandler(errorHandler);
    }
    public void write(final String string){
        try{
            super.out.write(string);
        }
        catch(IOException e){
            this.errorHandler.error("Failed to write ["+string+"].",e,1);
        }
    }
    public void flush(){
        try{
            super.out.flush();
        }
        catch(IOException e){
            this.errorHandler.error("Failed to flush writer,",e,2);
        }
    }
    public void setErrorHandler(final ErrorHandler eh){
        if(eh==null){
            throw new IllegalArgumentException("Attempted to set null ErrorHandler.");
        }
        this.errorHandler=eh;
    }
}
