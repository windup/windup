package org.apache.log4j;

import java.util.Enumeration;
import org.apache.log4j.helpers.LogLog;
import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;

public class NDC{
    static Hashtable ht;
    static int pushCounter;
    static final int REAP_THRESHOLD=5;
    public static void clear(){
        final Stack stack=NDC.ht.get(Thread.currentThread());
        if(stack!=null){
            stack.setSize(0);
        }
    }
    public static Stack cloneStack(){
        final Object o=NDC.ht.get(Thread.currentThread());
        if(o==null){
            return null;
        }
        final Stack stack=(Stack)o;
        return (Stack)stack.clone();
    }
    public static void inherit(final Stack stack){
        if(stack!=null){
            NDC.ht.put(Thread.currentThread(),stack);
        }
    }
    public static String get(){
        final Stack s=NDC.ht.get(Thread.currentThread());
        if(s!=null&&!s.isEmpty()){
            return s.peek().fullMessage;
        }
        return null;
    }
    public static int getDepth(){
        final Stack stack=NDC.ht.get(Thread.currentThread());
        if(stack==null){
            return 0;
        }
        return stack.size();
    }
    private static void lazyRemove(){
        final Vector v;
        synchronized(NDC.ht){
            if(++NDC.pushCounter<=5){
                return;
            }
            NDC.pushCounter=0;
            int misses=0;
            v=new Vector();
            final Enumeration enum1=NDC.ht.keys();
            while(enum1.hasMoreElements()&&misses<=4){
                final Thread t=enum1.nextElement();
                if(t.isAlive()){
                    ++misses;
                }
                else{
                    misses=0;
                    v.addElement(t);
                }
            }
        }
        for(int size=v.size(),i=0;i<size;++i){
            final Thread t=v.elementAt(i);
            LogLog.debug("Lazy NDC removal for thread ["+t.getName()+"] ("+NDC.ht.size()+").");
            NDC.ht.remove(t);
        }
    }
    public static String pop(){
        final Thread key=Thread.currentThread();
        final Stack stack=NDC.ht.get(key);
        if(stack!=null&&!stack.isEmpty()){
            return stack.pop().message;
        }
        return "";
    }
    public static String peek(){
        final Thread key=Thread.currentThread();
        final Stack stack=NDC.ht.get(key);
        if(stack!=null&&!stack.isEmpty()){
            return stack.peek().message;
        }
        return "";
    }
    public static void push(final String message){
        final Thread key=Thread.currentThread();
        Stack stack=NDC.ht.get(key);
        if(stack==null){
            final DiagnosticContext dc=new DiagnosticContext(message,null);
            stack=new Stack();
            NDC.ht.put(key,stack);
            stack.push(dc);
        }
        else if(stack.isEmpty()){
            final DiagnosticContext dc=new DiagnosticContext(message,null);
            stack.push(dc);
        }
        else{
            final DiagnosticContext parent=stack.peek();
            stack.push(new DiagnosticContext(message,parent));
        }
    }
    public static void remove(){
        NDC.ht.remove(Thread.currentThread());
        lazyRemove();
    }
    public static void setMaxDepth(final int maxDepth){
        final Stack stack=NDC.ht.get(Thread.currentThread());
        if(stack!=null&&maxDepth<stack.size()){
            stack.setSize(maxDepth);
        }
    }
    static{
        NDC.ht=new Hashtable();
        NDC.pushCounter=0;
    }
    private static class DiagnosticContext{
        String fullMessage;
        String message;
        DiagnosticContext(final String message,final DiagnosticContext parent){
            super();
            this.message=message;
            if(parent!=null){
                this.fullMessage=parent.fullMessage+' '+message;
            }
            else{
                this.fullMessage=message;
            }
        }
    }
}
