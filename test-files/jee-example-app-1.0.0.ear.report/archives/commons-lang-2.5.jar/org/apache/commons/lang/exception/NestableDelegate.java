package org.apache.commons.lang.exception;

import java.util.Collection;
import java.util.Arrays;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.exception.Nestable;
import java.io.Serializable;

public class NestableDelegate implements Serializable{
    private static final long serialVersionUID=1L;
    private static final transient String MUST_BE_THROWABLE="The Nestable implementation passed to the NestableDelegate(Nestable) constructor must extend java.lang.Throwable";
    private Throwable nestable;
    public static boolean topDown;
    public static boolean trimStackFrames;
    public static boolean matchSubclasses;
    static /* synthetic */ Class class$org$apache$commons$lang$exception$Nestable;
    public NestableDelegate(final Nestable nestable){
        super();
        this.nestable=null;
        if(nestable instanceof Throwable){
            this.nestable=(Throwable)nestable;
            return;
        }
        throw new IllegalArgumentException("The Nestable implementation passed to the NestableDelegate(Nestable) constructor must extend java.lang.Throwable");
    }
    public String getMessage(final int index){
        final Throwable t=this.getThrowable(index);
        if(((NestableDelegate.class$org$apache$commons$lang$exception$Nestable==null)?(NestableDelegate.class$org$apache$commons$lang$exception$Nestable=class$("org.apache.commons.lang.exception.Nestable")):NestableDelegate.class$org$apache$commons$lang$exception$Nestable).isInstance(t)){
            return ((Nestable)t).getMessage(0);
        }
        return t.getMessage();
    }
    public String getMessage(final String baseMsg){
        final Throwable nestedCause=ExceptionUtils.getCause(this.nestable);
        final String causeMsg=(nestedCause==null)?null:nestedCause.getMessage();
        if(nestedCause==null||causeMsg==null){
            return baseMsg;
        }
        if(baseMsg==null){
            return causeMsg;
        }
        return baseMsg+": "+causeMsg;
    }
    public String[] getMessages(){
        final Throwable[] throwables=this.getThrowables();
        final String[] msgs=new String[throwables.length];
        for(int i=0;i<throwables.length;++i){
            msgs[i]=(((NestableDelegate.class$org$apache$commons$lang$exception$Nestable==null)?(NestableDelegate.class$org$apache$commons$lang$exception$Nestable=class$("org.apache.commons.lang.exception.Nestable")):NestableDelegate.class$org$apache$commons$lang$exception$Nestable).isInstance(throwables[i])?((Nestable)throwables[i]).getMessage(0):throwables[i].getMessage());
        }
        return msgs;
    }
    public Throwable getThrowable(final int index){
        if(index==0){
            return this.nestable;
        }
        final Throwable[] throwables=this.getThrowables();
        return throwables[index];
    }
    public int getThrowableCount(){
        return ExceptionUtils.getThrowableCount(this.nestable);
    }
    public Throwable[] getThrowables(){
        return ExceptionUtils.getThrowables(this.nestable);
    }
    public int indexOfThrowable(final Class type,final int fromIndex){
        if(type==null){
            return -1;
        }
        if(fromIndex<0){
            throw new IndexOutOfBoundsException("The start index was out of bounds: "+fromIndex);
        }
        final Throwable[] throwables=ExceptionUtils.getThrowables(this.nestable);
        if(fromIndex>=throwables.length){
            throw new IndexOutOfBoundsException("The start index was out of bounds: "+fromIndex+" >= "+throwables.length);
        }
        if(NestableDelegate.matchSubclasses){
            for(int i=fromIndex;i<throwables.length;++i){
                if(type.isAssignableFrom(throwables[i].getClass())){
                    return i;
                }
            }
        }
        else{
            for(int i=fromIndex;i<throwables.length;++i){
                if(type.equals(throwables[i].getClass())){
                    return i;
                }
            }
        }
        return -1;
    }
    public void printStackTrace(){
        this.printStackTrace(System.err);
    }
    public void printStackTrace(final PrintStream out){
        synchronized(out){
            final PrintWriter pw=new PrintWriter(out,false);
            this.printStackTrace(pw);
            pw.flush();
        }
    }
    public void printStackTrace(final PrintWriter out){
        Throwable throwable=this.nestable;
        if(ExceptionUtils.isThrowableNested()){
            if(throwable instanceof Nestable){
                ((Nestable)throwable).printPartialStackTrace(out);
            }
            else{
                throwable.printStackTrace(out);
            }
            return;
        }
        final List stacks=new ArrayList();
        while(throwable!=null){
            final String[] st=this.getStackFrames(throwable);
            stacks.add(st);
            throwable=ExceptionUtils.getCause(throwable);
        }
        String separatorLine="Caused by: ";
        if(!NestableDelegate.topDown){
            separatorLine="Rethrown as: ";
            Collections.reverse(stacks);
        }
        if(NestableDelegate.trimStackFrames){
            this.trimStackFrames(stacks);
        }
        synchronized(out){
            final Iterator iter=stacks.iterator();
            while(iter.hasNext()){
                final String[] st2=iter.next();
                for(int i=0,len=st2.length;i<len;++i){
                    out.println(st2[i]);
                }
                if(iter.hasNext()){
                    out.print(separatorLine);
                }
            }
        }
    }
    protected String[] getStackFrames(final Throwable t){
        final StringWriter sw=new StringWriter();
        final PrintWriter pw=new PrintWriter(sw,true);
        if(t instanceof Nestable){
            ((Nestable)t).printPartialStackTrace(pw);
        }
        else{
            t.printStackTrace(pw);
        }
        return ExceptionUtils.getStackFrames(sw.getBuffer().toString());
    }
    protected void trimStackFrames(final List stacks){
        final int size=stacks.size();
        for(int i=size-1;i>0;--i){
            final String[] curr=stacks.get(i);
            final String[] next=stacks.get(i-1);
            final List currList=new ArrayList(Arrays.asList(curr));
            final List nextList=new ArrayList(Arrays.asList(next));
            ExceptionUtils.removeCommonFrames(currList,nextList);
            final int trimmed=curr.length-currList.size();
            if(trimmed>0){
                currList.add("\t... "+trimmed+" more");
                stacks.set(i,currList.toArray(new String[currList.size()]));
            }
        }
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        NestableDelegate.topDown=true;
        NestableDelegate.trimStackFrames=true;
        NestableDelegate.matchSubclasses=true;
    }
}
