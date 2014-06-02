package org.apache.wicket;

import org.apache.wicket.request.cycle.*;

public class ThreadContext{
    private Application application;
    private RequestCycle requestCycle;
    private Session session;
    private static final ThreadLocal<ThreadContext> threadLocal;
    public static ThreadContext get(final boolean createIfDoesNotExist){
        ThreadContext context=(ThreadContext)ThreadContext.threadLocal.get();
        if(context==null){
            if(createIfDoesNotExist){
                context=new ThreadContext();
                ThreadContext.threadLocal.set(context);
            }
            else{
                ThreadContext.threadLocal.remove();
            }
        }
        return context;
    }
    public static boolean exists(){
        return get(false)!=null;
    }
    public static Application getApplication(){
        final ThreadContext context=get(false);
        return (context!=null)?context.application:null;
    }
    public static void setApplication(final Application application){
        final ThreadContext context=get(true);
        context.application=application;
    }
    public static RequestCycle getRequestCycle(){
        final ThreadContext context=get(false);
        return (context!=null)?context.requestCycle:null;
    }
    public static void setRequestCycle(final RequestCycle requestCycle){
        final ThreadContext context=get(true);
        context.requestCycle=requestCycle;
    }
    public static Session getSession(){
        final ThreadContext context=get(false);
        return (context!=null)?context.session:null;
    }
    public static void setSession(final Session session){
        final ThreadContext context=get(true);
        context.session=session;
    }
    public static ThreadContext detach(){
        final ThreadContext value=(ThreadContext)ThreadContext.threadLocal.get();
        ThreadContext.threadLocal.remove();
        return value;
    }
    public static void restore(final ThreadContext threadContext){
        if(threadContext==null){
            ThreadContext.threadLocal.remove();
        }
        else{
            ThreadContext.threadLocal.set(threadContext);
        }
    }
    static{
        threadLocal=new ThreadLocal();
    }
}
