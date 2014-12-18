package org.apache.log4j.jmx;

import javax.management.InvalidAttributeValueException;
import org.apache.log4j.helpers.OptionConverter;
import javax.management.Attribute;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import org.apache.log4j.Appender;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.NotificationFilterSupport;
import org.apache.log4j.jmx.LoggerDynamicMBean;
import org.apache.log4j.Category;
import javax.management.ObjectName;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanAttributeInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import javax.management.NotificationBroadcasterSupport;
import java.util.Vector;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.NotificationBroadcaster;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.jmx.AbstractDynamicMBean;

public class HierarchyDynamicMBean extends AbstractDynamicMBean implements HierarchyEventListener,NotificationBroadcaster{
    static final String ADD_APPENDER="addAppender.";
    static final String THRESHOLD="threshold";
    private MBeanConstructorInfo[] dConstructors;
    private MBeanOperationInfo[] dOperations;
    private Vector vAttributes;
    private String dClassName;
    private String dDescription;
    private NotificationBroadcasterSupport nbs;
    private LoggerRepository hierarchy;
    private static Logger log;
    static /* synthetic */ Class class$org$apache$log4j$jmx$HierarchyDynamicMBean;
    public HierarchyDynamicMBean(){
        super();
        this.dConstructors=new MBeanConstructorInfo[1];
        this.dOperations=new MBeanOperationInfo[1];
        this.vAttributes=new Vector();
        this.dClassName=this.getClass().getName();
        this.dDescription="This MBean acts as a management facade for org.apache.log4j.Hierarchy.";
        this.nbs=new NotificationBroadcasterSupport();
        this.hierarchy=LogManager.getLoggerRepository();
        this.buildDynamicMBeanInfo();
    }
    private void buildDynamicMBeanInfo(){
        this.dConstructors[0]=new MBeanConstructorInfo("HierarchyDynamicMBean(): Constructs a HierarchyDynamicMBean instance",this.getClass().getConstructors()[0]);
        this.vAttributes.add(new MBeanAttributeInfo("threshold","java.lang.String","The \"threshold\" state of the hiearchy.",true,true,false));
        this.dOperations[0]=new MBeanOperationInfo("addLoggerMBean","addLoggerMBean(): add a loggerMBean",new MBeanParameterInfo[] { new MBeanParameterInfo("name","java.lang.String","Create a logger MBean") },"javax.management.ObjectName",1);
    }
    public ObjectName addLoggerMBean(final String name){
        final Logger exists=Category.exists(name);
        if(exists!=null){
            return this.addLoggerMBean(exists);
        }
        return null;
    }
    ObjectName addLoggerMBean(final Logger logger){
        final String name=logger.getName();
        ObjectName objectName=null;
        try{
            final LoggerDynamicMBean loggerDynamicMBean=new LoggerDynamicMBean(logger);
            objectName=new ObjectName("log4j","logger",name);
            super.server.registerMBean(loggerDynamicMBean,objectName);
            final NotificationFilterSupport notificationFilterSupport=new NotificationFilterSupport();
            notificationFilterSupport.enableType("addAppender."+logger.getName());
            HierarchyDynamicMBean.log.debug("---Adding logger ["+name+"] as listener.");
            this.nbs.addNotificationListener(loggerDynamicMBean,notificationFilterSupport,null);
            this.vAttributes.add(new MBeanAttributeInfo("logger="+name,"javax.management.ObjectName","The "+name+" logger.",true,true,false));
        }
        catch(Exception ex){
            HierarchyDynamicMBean.log.error("Couls not add loggerMBean for ["+name+"].");
        }
        return objectName;
    }
    public void addNotificationListener(final NotificationListener notificationListener,final NotificationFilter notificationFilter,final Object o){
        this.nbs.addNotificationListener(notificationListener,notificationFilter,o);
    }
    protected Logger getLogger(){
        return HierarchyDynamicMBean.log;
    }
    public MBeanInfo getMBeanInfo(){
        final MBeanAttributeInfo[] array=new MBeanAttributeInfo[this.vAttributes.size()];
        this.vAttributes.toArray(array);
        return new MBeanInfo(this.dClassName,this.dDescription,array,this.dConstructors,this.dOperations,new MBeanNotificationInfo[0]);
    }
    public MBeanNotificationInfo[] getNotificationInfo(){
        return this.nbs.getNotificationInfo();
    }
    public Object invoke(final String s,final Object[] array,final String[] array2) throws MBeanException,ReflectionException{
        if(s==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),"Cannot invoke a null operation in "+this.dClassName);
        }
        if(s.equals("addLoggerMBean")){
            return this.addLoggerMBean((String)array[0]);
        }
        throw new ReflectionException(new NoSuchMethodException(s),"Cannot find the operation "+s+" in "+this.dClassName);
    }
    public Object getAttribute(final String s) throws AttributeNotFoundException,MBeanException,ReflectionException{
        if(s==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),"Cannot invoke a getter of "+this.dClassName+" with null attribute name");
        }
        HierarchyDynamicMBean.log.debug("Called getAttribute with ["+s+"].");
        if(s.equals("threshold")){
            return this.hierarchy.getThreshold();
        }
        if(s.startsWith("logger")){
            final int index=s.indexOf("%3D");
            String string=s;
            if(index>0){
                string=s.substring(0,index)+'='+s.substring(index+3);
            }
            try{
                return new ObjectName("log4j:"+string);
            }
            catch(Exception ex){
                HierarchyDynamicMBean.log.error("Could not create ObjectName"+string);
            }
        }
        throw new AttributeNotFoundException("Cannot find "+s+" attribute in "+this.dClassName);
    }
    public void addAppenderEvent(final Category category,final Appender userData){
        HierarchyDynamicMBean.log.debug("addAppenderEvent called: logger="+category.getName()+", appender="+userData.getName());
        final Notification notification=new Notification("addAppender."+category.getName(),this,0L);
        notification.setUserData(userData);
        HierarchyDynamicMBean.log.debug("sending notification.");
        this.nbs.sendNotification(notification);
    }
    public void removeAppenderEvent(final Category category,final Appender appender){
        HierarchyDynamicMBean.log.debug("removeAppenderCalled: logger="+category.getName()+", appender="+appender.getName());
    }
    public void postRegister(final Boolean b){
        HierarchyDynamicMBean.log.debug("postRegister is called.");
        this.hierarchy.addHierarchyEventListener(this);
        this.addLoggerMBean(this.hierarchy.getRootLogger());
    }
    public void removeNotificationListener(final NotificationListener notificationListener) throws ListenerNotFoundException{
        this.nbs.removeNotificationListener(notificationListener);
    }
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException,InvalidAttributeValueException,MBeanException,ReflectionException{
        if(attribute==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"),"Cannot invoke a setter of "+this.dClassName+" with null attribute");
        }
        final String name=attribute.getName();
        final Object value=attribute.getValue();
        if(name==null){
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),"Cannot invoke the setter of "+this.dClassName+" with null attribute name");
        }
        if(name.equals("threshold")){
            this.hierarchy.setThreshold(OptionConverter.toLevel((String)value,this.hierarchy.getThreshold()));
        }
    }
    static /* synthetic */ Class class$(final String s){
        try{
            return Class.forName(s);
        }
        catch(ClassNotFoundException ex){
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }
    static{
        HierarchyDynamicMBean.log=Logger.getLogger((HierarchyDynamicMBean.class$org$apache$log4j$jmx$HierarchyDynamicMBean==null)?(HierarchyDynamicMBean.class$org$apache$log4j$jmx$HierarchyDynamicMBean=class$("org.apache.log4j.jmx.HierarchyDynamicMBean")):HierarchyDynamicMBean.class$org$apache$log4j$jmx$HierarchyDynamicMBean);
    }
}
