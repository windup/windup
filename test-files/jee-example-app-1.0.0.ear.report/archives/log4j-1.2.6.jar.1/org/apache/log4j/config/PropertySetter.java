package org.apache.log4j.config;

import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.Level;
import org.apache.log4j.config.PropertySetterException;
import java.util.Enumeration;
import org.apache.log4j.Appender;
import org.apache.log4j.helpers.OptionConverter;
import java.util.Properties;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import org.apache.log4j.helpers.LogLog;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class PropertySetter{
    protected Object obj;
    protected PropertyDescriptor[] props;
    static /* synthetic */ Class class$java$lang$String;
    static /* synthetic */ Class class$org$apache$log4j$Priority;
    public PropertySetter(final Object obj){
        super();
        this.obj=obj;
    }
    protected void introspect(){
        try{
            final BeanInfo bi=Introspector.getBeanInfo(this.obj.getClass());
            this.props=bi.getPropertyDescriptors();
        }
        catch(IntrospectionException ex){
            LogLog.error("Failed to introspect "+this.obj+": "+ex.getMessage());
            this.props=new PropertyDescriptor[0];
        }
    }
    public static void setProperties(final Object obj,final Properties properties,final String prefix){
        new PropertySetter(obj).setProperties(properties,prefix);
    }
    public void setProperties(final Properties properties,final String prefix){
        final int len=prefix.length();
        final Enumeration e=properties.propertyNames();
        while(e.hasMoreElements()){
            String key=e.nextElement();
            if(key.startsWith(prefix)){
                if(key.indexOf(46,len+1)>0){
                    continue;
                }
                final String value=OptionConverter.findAndSubst(key,properties);
                key=key.substring(len);
                if("layout".equals(key)&&this.obj instanceof Appender){
                    continue;
                }
                this.setProperty(key,value);
            }
        }
        this.activate();
    }
    public void setProperty(String name,final String value){
        if(value==null){
            return;
        }
        name=Introspector.decapitalize(name);
        final PropertyDescriptor prop=this.getPropertyDescriptor(name);
        if(prop==null){
            LogLog.warn("No such property ["+name+"] in "+this.obj.getClass().getName()+".");
        }
        else{
            try{
                this.setProperty(prop,name,value);
            }
            catch(PropertySetterException ex){
                LogLog.warn("Failed to set property ["+name+"] to value \""+value+"\". ",ex.rootCause);
            }
        }
    }
    public void setProperty(final PropertyDescriptor prop,final String name,final String value) throws PropertySetterException{
        final Method setter=prop.getWriteMethod();
        if(setter==null){
            throw new PropertySetterException("No setter for property ["+name+"].");
        }
        final Class[] paramTypes=setter.getParameterTypes();
        if(paramTypes.length!=1){
            throw new PropertySetterException("#params for setter != 1");
        }
        Object arg;
        try{
            arg=this.convertArg(value,paramTypes[0]);
        }
        catch(Throwable t){
            throw new PropertySetterException("Conversion to type ["+paramTypes[0]+"] failed. Reason: "+t);
        }
        if(arg==null){
            throw new PropertySetterException("Conversion to type ["+paramTypes[0]+"] failed.");
        }
        LogLog.debug("Setting property ["+name+"] to ["+arg+"].");
        try{
            setter.invoke(this.obj,arg);
        }
        catch(Exception ex){
            throw new PropertySetterException(ex);
        }
    }
    protected Object convertArg(final String val,final Class type){
        if(val==null){
            return null;
        }
        final String v=val.trim();
        if(((PropertySetter.class$java$lang$String==null)?(PropertySetter.class$java$lang$String=class$("java.lang.String")):PropertySetter.class$java$lang$String).isAssignableFrom(type)){
            return val;
        }
        if(Integer.TYPE.isAssignableFrom(type)){
            return new Integer(v);
        }
        if(Long.TYPE.isAssignableFrom(type)){
            return new Long(v);
        }
        if(Boolean.TYPE.isAssignableFrom(type)){
            if("true".equalsIgnoreCase(v)){
                return Boolean.TRUE;
            }
            if("false".equalsIgnoreCase(v)){
                return Boolean.FALSE;
            }
        }
        else if(((PropertySetter.class$org$apache$log4j$Priority==null)?(PropertySetter.class$org$apache$log4j$Priority=class$("org.apache.log4j.Priority")):PropertySetter.class$org$apache$log4j$Priority).isAssignableFrom(type)){
            return OptionConverter.toLevel(v,Level.DEBUG);
        }
        return null;
    }
    protected PropertyDescriptor getPropertyDescriptor(final String name){
        if(this.props==null){
            this.introspect();
        }
        for(int i=0;i<this.props.length;++i){
            if(name.equals(this.props[i].getName())){
                return this.props[i];
            }
        }
        return null;
    }
    public void activate(){
        if(this.obj instanceof OptionHandler){
            ((OptionHandler)this.obj).activateOptions();
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
}
