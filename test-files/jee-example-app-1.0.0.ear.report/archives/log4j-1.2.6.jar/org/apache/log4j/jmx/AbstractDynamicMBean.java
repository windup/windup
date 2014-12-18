package org.apache.log4j.jmx;

import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.InvalidAttributeValueException;
import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;
import org.apache.log4j.Logger;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.RuntimeOperationsException;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.DynamicMBean;

public abstract class AbstractDynamicMBean implements DynamicMBean,MBeanRegistration{
    String dClassName;
    MBeanServer server;
    public AttributeList getAttributes(final String[] array){
        if(array==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),"Cannot invoke a getter of "+this.dClassName);
        }
        final AttributeList list=new AttributeList();
        if(array.length==0){
            return list;
        }
        for(int i=0;i<array.length;++i){
            try{
                list.add(new Attribute(array[i],this.getAttribute(array[i])));
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return list;
    }
    public AttributeList setAttributes(final AttributeList list){
        if(list==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("AttributeList attributes cannot be null"),"Cannot invoke a setter of "+this.dClassName);
        }
        final AttributeList list2=new AttributeList();
        if(list.isEmpty()){
            return list2;
        }
        for(final Attribute attribute : list){
            try{
                this.setAttribute(attribute);
                final String name=attribute.getName();
                list2.add(new Attribute(name,this.getAttribute(name)));
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return list2;
    }
    protected abstract Logger getLogger();
    public void postDeregister(){
        this.getLogger().debug("postDeregister is called.");
    }
    public void postRegister(final Boolean b){
    }
    public void preDeregister(){
        this.getLogger().debug("preDeregister called.");
    }
    public ObjectName preRegister(final MBeanServer server,final ObjectName objectName){
        this.getLogger().debug("preRegister called. Server="+server+", name="+objectName);
        this.server=server;
        return objectName;
    }
    public abstract void setAttribute(final Attribute p0) throws AttributeNotFoundException,InvalidAttributeValueException,MBeanException,ReflectionException;
    public abstract Object invoke(final String p0,final Object[] p1,final String[] p2) throws MBeanException,ReflectionException;
    public abstract MBeanInfo getMBeanInfo();
    public abstract Object getAttribute(final String p0) throws AttributeNotFoundException,MBeanException,ReflectionException;
}
