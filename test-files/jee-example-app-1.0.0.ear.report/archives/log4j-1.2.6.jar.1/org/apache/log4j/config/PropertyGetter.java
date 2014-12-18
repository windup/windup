package org.apache.log4j.config;

import org.apache.log4j.helpers.LogLog;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class PropertyGetter{
    protected static final Object[] NULL_ARG;
    protected Object obj;
    protected PropertyDescriptor[] props;
    static /* synthetic */ Class class$java$lang$String;
    static /* synthetic */ Class class$org$apache$log4j$Priority;
    public PropertyGetter(final Object obj) throws IntrospectionException{
        super();
        final BeanInfo bi=Introspector.getBeanInfo(obj.getClass());
        this.props=bi.getPropertyDescriptors();
        this.obj=obj;
    }
    public static void getProperties(final Object obj,final PropertyCallback callback,final String prefix){
        try{
            new PropertyGetter(obj).getProperties(callback,prefix);
        }
        catch(IntrospectionException ex){
            LogLog.error("Failed to introspect object "+obj,ex);
        }
    }
    public void getProperties(final PropertyCallback callback,final String prefix){
        for(int i=0;i<this.props.length;++i){
            final Method getter=this.props[i].getReadMethod();
            if(getter!=null){
                if(this.isHandledType(getter.getReturnType())){
                    final String name=this.props[i].getName();
                    try{
                        final Object result=getter.invoke(this.obj,PropertyGetter.NULL_ARG);
                        if(result!=null){
                            callback.foundProperty(this.obj,prefix,name,result);
                        }
                    }
                    catch(Exception ex){
                        LogLog.warn("Failed to get value of property "+name);
                    }
                }
            }
        }
    }
    protected boolean isHandledType(final Class type){
        return ((PropertyGetter.class$java$lang$String==null)?(PropertyGetter.class$java$lang$String=class$("java.lang.String")):PropertyGetter.class$java$lang$String).isAssignableFrom(type)||Integer.TYPE.isAssignableFrom(type)||Long.TYPE.isAssignableFrom(type)||Boolean.TYPE.isAssignableFrom(type)||((PropertyGetter.class$org$apache$log4j$Priority==null)?(PropertyGetter.class$org$apache$log4j$Priority=class$("org.apache.log4j.Priority")):PropertyGetter.class$org$apache$log4j$Priority).isAssignableFrom(type);
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
        NULL_ARG=new Object[0];
    }
    public interface PropertyCallback{
        void foundProperty(Object p0,String p1,String p2,Object p3);
    }
}
