package org.apache.log4j;

import java.util.Hashtable;
import org.apache.log4j.helpers.ThreadLocalMap;
import org.apache.log4j.helpers.Loader;

public class MDC{
    static final MDC mdc;
    static final int HT_SIZE=7;
    boolean java1;
    Object tlm;
    private MDC(){
        super();
        if(!(this.java1=Loader.isJava1())){
            this.tlm=new ThreadLocalMap();
        }
    }
    public static void put(final String key,final Object o){
        MDC.mdc.put0(key,o);
    }
    public static Object get(final String key){
        return MDC.mdc.get0(key);
    }
    public static void remove(final String key){
        MDC.mdc.remove0(key);
    }
    public static Hashtable getContext(){
        return MDC.mdc.getContext0();
    }
    private void put0(final String key,final Object o){
        if(this.java1){
            return;
        }
        Hashtable ht=((ThreadLocalMap)this.tlm).get();
        if(ht==null){
            ht=new Hashtable(7);
            ((ThreadLocalMap)this.tlm).set(ht);
        }
        ht.put(key,o);
    }
    private Object get0(final String key){
        if(this.java1){
            return null;
        }
        final Hashtable ht=((ThreadLocalMap)this.tlm).get();
        if(ht!=null&&key!=null){
            return ht.get(key);
        }
        return null;
    }
    private void remove0(final String key){
        if(!this.java1){
            final Hashtable ht=((ThreadLocalMap)this.tlm).get();
            if(ht!=null){
                ht.remove(key);
            }
        }
    }
    private Hashtable getContext0(){
        if(this.java1){
            return null;
        }
        return ((ThreadLocalMap)this.tlm).get();
    }
    static{
        mdc=new MDC();
    }
}
