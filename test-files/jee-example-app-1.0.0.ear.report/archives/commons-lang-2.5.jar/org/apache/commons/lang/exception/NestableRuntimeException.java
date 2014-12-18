package org.apache.commons.lang.exception;

import java.io.PrintWriter;
import java.io.PrintStream;
import org.apache.commons.lang.exception.NestableDelegate;
import org.apache.commons.lang.exception.Nestable;

public class NestableRuntimeException extends RuntimeException implements Nestable{
    private static final long serialVersionUID=1L;
    protected NestableDelegate delegate;
    private Throwable cause;
    public NestableRuntimeException(){
        super();
        this.delegate=new NestableDelegate(this);
        this.cause=null;
    }
    public NestableRuntimeException(final String msg){
        super(msg);
        this.delegate=new NestableDelegate(this);
        this.cause=null;
    }
    public NestableRuntimeException(final Throwable cause){
        super();
        this.delegate=new NestableDelegate(this);
        this.cause=null;
        this.cause=cause;
    }
    public NestableRuntimeException(final String msg,final Throwable cause){
        super(msg);
        this.delegate=new NestableDelegate(this);
        this.cause=null;
        this.cause=cause;
    }
    public Throwable getCause(){
        return this.cause;
    }
    public String getMessage(){
        if(super.getMessage()!=null){
            return super.getMessage();
        }
        if(this.cause!=null){
            return this.cause.toString();
        }
        return null;
    }
    public String getMessage(final int index){
        if(index==0){
            return super.getMessage();
        }
        return this.delegate.getMessage(index);
    }
    public String[] getMessages(){
        return this.delegate.getMessages();
    }
    public Throwable getThrowable(final int index){
        return this.delegate.getThrowable(index);
    }
    public int getThrowableCount(){
        return this.delegate.getThrowableCount();
    }
    public Throwable[] getThrowables(){
        return this.delegate.getThrowables();
    }
    public int indexOfThrowable(final Class type){
        return this.delegate.indexOfThrowable(type,0);
    }
    public int indexOfThrowable(final Class type,final int fromIndex){
        return this.delegate.indexOfThrowable(type,fromIndex);
    }
    public void printStackTrace(){
        this.delegate.printStackTrace();
    }
    public void printStackTrace(final PrintStream out){
        this.delegate.printStackTrace(out);
    }
    public void printStackTrace(final PrintWriter out){
        this.delegate.printStackTrace(out);
    }
    public final void printPartialStackTrace(final PrintWriter out){
        super.printStackTrace(out);
    }
}
