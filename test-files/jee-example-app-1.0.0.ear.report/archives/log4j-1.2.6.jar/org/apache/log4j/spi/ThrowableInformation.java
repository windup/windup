package org.apache.log4j.spi;

import java.io.PrintWriter;
import org.apache.log4j.spi.VectorWriter;
import java.io.Serializable;

public class ThrowableInformation implements Serializable{
    static final long serialVersionUID=-4748765566864322735L;
    private transient Throwable throwable;
    private String[] rep;
    public ThrowableInformation(final Throwable throwable){
        super();
        this.throwable=throwable;
    }
    public Throwable getThrowable(){
        return this.throwable;
    }
    public String[] getThrowableStrRep(){
        if(this.rep!=null){
            return this.rep.clone();
        }
        final VectorWriter vw=new VectorWriter();
        this.throwable.printStackTrace(vw);
        return this.rep=vw.toStringArray();
    }
}
