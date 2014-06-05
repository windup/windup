package org.apache.wicket.request.resource;

import java.util.*;
import org.apache.wicket.util.collections.*;
import java.lang.reflect.*;
import org.slf4j.*;

abstract class ClassScanner{
    private static Logger log;
    private final Set<String> scannedClasses;
    abstract boolean foundResourceReference(final ResourceReference p0);
    ClassScanner(){
        super();
        this.scannedClasses=(Set<String>)new ConcurrentHashSet();
    }
    public final void clearCache(){
        this.scannedClasses.clear();
    }
    public int scanClass(final Class<?> klass){
        if(klass==null){
            return 0;
        }
        int count=0;
        final String className=klass.getName();
        if(!this.scannedClasses.contains(className)){
            this.scannedClasses.add(className);
            for(final Field f : klass.getDeclaredFields()){
                if((f.getModifiers()&0x8)==0x8){
                    f.setAccessible(true);
                    try{
                        final Object value=f.get(null);
                        if(value instanceof ResourceReference&&this.foundResourceReference((ResourceReference)value)){
                            ++count;
                        }
                    }
                    catch(Exception e){
                        ClassScanner.log.warn("Error accessing object property",e);
                    }
                }
            }
            count+=this.scanClass((Class<?>)klass.getSuperclass());
        }
        return count;
    }
    static{
        ClassScanner.log=LoggerFactory.getLogger(ClassScanner.class);
    }
}
