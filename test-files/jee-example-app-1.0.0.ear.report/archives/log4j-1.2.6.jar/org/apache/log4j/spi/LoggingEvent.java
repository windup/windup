package org.apache.log4j.spi;

import java.io.ObjectOutputStream;
import java.io.IOException;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.Loader;
import java.io.ObjectInputStream;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.ThrowableInformation;
import java.util.Hashtable;
import org.apache.log4j.Priority;
import org.apache.log4j.Category;
import java.io.Serializable;

public class LoggingEvent implements Serializable{
    private static long startTime;
    public final transient String fqnOfCategoryClass;
    private transient Category logger;
    public final String categoryName;
    public transient Priority level;
    private String ndc;
    private Hashtable mdcCopy;
    private boolean ndcLookupRequired;
    private boolean mdcCopyLookupRequired;
    private transient Object message;
    private String renderedMessage;
    private String threadName;
    private ThrowableInformation throwableInfo;
    public final long timeStamp;
    private LocationInfo locationInfo;
    static final long serialVersionUID=-868428216207166145L;
    static final Integer[] PARAM_ARRAY;
    static final String TO_LEVEL="toLevel";
    static final Class[] TO_LEVEL_PARAMS;
    static final Hashtable methodCache;
    static /* synthetic */ Class class$org$apache$log4j$Level;
    public LoggingEvent(final String fqnOfCategoryClass,final Category logger,final Priority priority,final Object message,final Throwable throwable){
        super();
        this.ndcLookupRequired=true;
        this.mdcCopyLookupRequired=true;
        this.fqnOfCategoryClass=fqnOfCategoryClass;
        this.logger=logger;
        this.categoryName=logger.getName();
        this.level=priority;
        this.message=message;
        if(throwable!=null){
            this.throwableInfo=new ThrowableInformation(throwable);
        }
        this.timeStamp=System.currentTimeMillis();
    }
    public LoggingEvent(final String fqnOfCategoryClass,final Category logger,final long timeStamp,final Priority priority,final Object message,final Throwable throwable){
        super();
        this.ndcLookupRequired=true;
        this.mdcCopyLookupRequired=true;
        this.fqnOfCategoryClass=fqnOfCategoryClass;
        this.logger=logger;
        this.categoryName=logger.getName();
        this.level=priority;
        this.message=message;
        if(throwable!=null){
            this.throwableInfo=new ThrowableInformation(throwable);
        }
        this.timeStamp=timeStamp;
    }
    public LocationInfo getLocationInformation(){
        if(this.locationInfo==null){
            this.locationInfo=new LocationInfo(new Throwable(),this.fqnOfCategoryClass);
        }
        return this.locationInfo;
    }
    public Level getLevel(){
        return (Level)this.level;
    }
    public String getLoggerName(){
        return this.categoryName;
    }
    public Object getMessage(){
        if(this.message!=null){
            return this.message;
        }
        return this.getRenderedMessage();
    }
    public String getNDC(){
        if(this.ndcLookupRequired){
            this.ndcLookupRequired=false;
            this.ndc=NDC.get();
        }
        return this.ndc;
    }
    public Object getMDC(final String key){
        if(this.mdcCopy!=null){
            final Object r=this.mdcCopy.get(key);
            if(r!=null){
                return r;
            }
        }
        return MDC.get(key);
    }
    public void getMDCCopy(){
        if(this.mdcCopyLookupRequired){
            this.mdcCopyLookupRequired=false;
            final Hashtable t=MDC.getContext();
            if(t!=null){
                this.mdcCopy=(Hashtable)t.clone();
            }
        }
    }
    public String getRenderedMessage(){
        if(this.renderedMessage==null&&this.message!=null){
            if(this.message instanceof String){
                this.renderedMessage=(String)this.message;
            }
            else{
                final LoggerRepository repository=this.logger.getHierarchy();
                if(repository instanceof RendererSupport){
                    final RendererSupport rs=(RendererSupport)repository;
                    this.renderedMessage=rs.getRendererMap().findAndRender(this.message);
                }
                else{
                    this.renderedMessage=this.message.toString();
                }
            }
        }
        return this.renderedMessage;
    }
    public static long getStartTime(){
        return LoggingEvent.startTime;
    }
    public String getThreadName(){
        if(this.threadName==null){
            this.threadName=Thread.currentThread().getName();
        }
        return this.threadName;
    }
    public ThrowableInformation getThrowableInformation(){
        return this.throwableInfo;
    }
    public String[] getThrowableStrRep(){
        if(this.throwableInfo==null){
            return null;
        }
        return this.throwableInfo.getThrowableStrRep();
    }
    private void readLevel(final ObjectInputStream ois) throws IOException,ClassNotFoundException{
        final int p=ois.readInt();
        try{
            final String className=(String)ois.readObject();
            if(className==null){
                this.level=Level.toLevel(p);
            }
            else{
                Method m=LoggingEvent.methodCache.get(className);
                if(m==null){
                    final Class clazz=Loader.loadClass(className);
                    m=clazz.getDeclaredMethod("toLevel",(Class[])LoggingEvent.TO_LEVEL_PARAMS);
                    LoggingEvent.methodCache.put(className,m);
                }
                LoggingEvent.PARAM_ARRAY[0]=new Integer(p);
                this.level=(Level)m.invoke(null,(Object[])LoggingEvent.PARAM_ARRAY);
            }
        }
        catch(Exception e){
            LogLog.warn("Level deserialization failed, reverting to default.",e);
            this.level=Level.toLevel(p);
        }
    }
    private void readObject(final ObjectInputStream ois) throws IOException,ClassNotFoundException{
        ois.defaultReadObject();
        this.readLevel(ois);
        if(this.locationInfo==null){
            this.locationInfo=new LocationInfo(null,null);
        }
    }
    private void writeObject(final ObjectOutputStream oos) throws IOException{
        this.getThreadName();
        this.getRenderedMessage();
        this.getNDC();
        this.getMDCCopy();
        this.getThrowableStrRep();
        oos.defaultWriteObject();
        this.writeLevel(oos);
    }
    private void writeLevel(final ObjectOutputStream oos) throws IOException{
        oos.writeInt(this.level.toInt());
        final Class clazz=this.level.getClass();
        if(clazz==((LoggingEvent.class$org$apache$log4j$Level==null)?(LoggingEvent.class$org$apache$log4j$Level=class$("org.apache.log4j.Level")):LoggingEvent.class$org$apache$log4j$Level)){
            oos.writeObject(null);
        }
        else{
            oos.writeObject(clazz.getName());
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
        LoggingEvent.startTime=System.currentTimeMillis();
        PARAM_ARRAY=new Integer[1];
        TO_LEVEL_PARAMS=new Class[] { Integer.TYPE };
        methodCache=new Hashtable(3);
    }
}
