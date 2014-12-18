package org.apache.commons.lang;

import java.io.PrintWriter;
import java.io.PrintStream;
import org.apache.commons.lang.exception.NestableDelegate;
import org.apache.commons.lang.exception.Nestable;

public class NotImplementedException extends UnsupportedOperationException implements Nestable{
    private static final String DEFAULT_MESSAGE="Code is not implemented";
    private static final long serialVersionUID=-6894122266938754088L;
    private NestableDelegate delegate;
    private Throwable cause;
    public NotImplementedException(){
        super("Code is not implemented");
        this.delegate=new NestableDelegate(this);
    }
    public NotImplementedException(final String msg){
        super((msg==null)?"Code is not implemented":msg);
        this.delegate=new NestableDelegate(this);
    }
    public NotImplementedException(final Throwable cause){
        super("Code is not implemented");
        this.delegate=new NestableDelegate(this);
        this.cause=cause;
    }
    public NotImplementedException(final String msg,final Throwable cause){
        super((msg==null)?"Code is not implemented":msg);
        this.delegate=new NestableDelegate(this);
        this.cause=cause;
    }
    public NotImplementedException(final Class clazz){
        super((clazz==null)?"Code is not implemented":("Code is not implemented in "+clazz));
        this.delegate=new NestableDelegate(this);
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
