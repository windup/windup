package org.apache.log4j;

import java.util.ResourceBundle;
import java.util.Enumeration;
import org.apache.log4j.ProvisionNode;
import org.apache.log4j.Appender;
import org.apache.log4j.CategoryKey;
import org.apache.log4j.Category;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.or.ObjectRenderer;
import org.apache.log4j.DefaultCategoryFactory;
import org.apache.log4j.Level;
import org.apache.log4j.or.RendererMap;
import org.apache.log4j.Logger;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.spi.LoggerRepository;

public class Hierarchy implements LoggerRepository,RendererSupport{
    private LoggerFactory defaultFactory;
    private Vector listeners;
    Hashtable ht;
    Logger root;
    RendererMap rendererMap;
    int thresholdInt;
    Level threshold;
    boolean emittedNoAppenderWarning;
    boolean emittedNoResourceBundleWarning;
    public Hierarchy(final Logger root){
        super();
        this.emittedNoAppenderWarning=false;
        this.emittedNoResourceBundleWarning=false;
        this.ht=new Hashtable();
        this.listeners=new Vector(1);
        this.root=root;
        this.setThreshold(Level.ALL);
        this.root.setHierarchy(this);
        this.rendererMap=new RendererMap();
        this.defaultFactory=new DefaultCategoryFactory();
    }
    public void addRenderer(final Class classToRender,final ObjectRenderer or){
        this.rendererMap.put(classToRender,or);
    }
    public void addHierarchyEventListener(final HierarchyEventListener listener){
        if(this.listeners.contains(listener)){
            LogLog.warn("Ignoring attempt to add an existent listener.");
        }
        else{
            this.listeners.addElement(listener);
        }
    }
    public void clear(){
        this.ht.clear();
    }
    public void emitNoAppenderWarning(final Category cat){
        if(!this.emittedNoAppenderWarning){
            LogLog.warn("No appenders could be found for logger ("+cat.getName()+").");
            LogLog.warn("Please initialize the log4j system properly.");
            this.emittedNoAppenderWarning=true;
        }
    }
    public Logger exists(final String name){
        final Object o=this.ht.get(new CategoryKey(name));
        if(o instanceof Logger){
            return (Logger)o;
        }
        return null;
    }
    public void setThreshold(final String levelStr){
        final Level l=Level.toLevel(levelStr,null);
        if(l!=null){
            this.setThreshold(l);
        }
        else{
            LogLog.warn("Could not convert ["+levelStr+"] to Level.");
        }
    }
    public void setThreshold(final Level l){
        if(l!=null){
            this.thresholdInt=l.level;
            this.threshold=l;
        }
    }
    public void fireAddAppenderEvent(final Category logger,final Appender appender){
        if(this.listeners!=null){
            for(int size=this.listeners.size(),i=0;i<size;++i){
                final HierarchyEventListener listener=this.listeners.elementAt(i);
                listener.addAppenderEvent(logger,appender);
            }
        }
    }
    void fireRemoveAppenderEvent(final Category logger,final Appender appender){
        if(this.listeners!=null){
            for(int size=this.listeners.size(),i=0;i<size;++i){
                final HierarchyEventListener listener=this.listeners.elementAt(i);
                listener.removeAppenderEvent(logger,appender);
            }
        }
    }
    public Level getThreshold(){
        return this.threshold;
    }
    public Logger getLogger(final String name){
        return this.getLogger(name,this.defaultFactory);
    }
    public Logger getLogger(final String name,final LoggerFactory factory){
        final CategoryKey key=new CategoryKey(name);
        synchronized(this.ht){
            final Object o=this.ht.get(key);
            if(o==null){
                final Logger logger=factory.makeNewLoggerInstance(name);
                logger.setHierarchy(this);
                this.ht.put(key,logger);
                this.updateParents(logger);
                return logger;
            }
            if(o instanceof Logger){
                return (Logger)o;
            }
            if(o instanceof ProvisionNode){
                final Logger logger=factory.makeNewLoggerInstance(name);
                logger.setHierarchy(this);
                this.ht.put(key,logger);
                this.updateChildren((ProvisionNode)o,logger);
                this.updateParents(logger);
                return logger;
            }
            return null;
        }
    }
    public Enumeration getCurrentLoggers(){
        final Vector v=new Vector(this.ht.size());
        final Enumeration elems=this.ht.elements();
        while(elems.hasMoreElements()){
            final Object o=elems.nextElement();
            if(o instanceof Logger){
                v.addElement(o);
            }
        }
        return v.elements();
    }
    public Enumeration getCurrentCategories(){
        return this.getCurrentLoggers();
    }
    public RendererMap getRendererMap(){
        return this.rendererMap;
    }
    public Logger getRootLogger(){
        return this.root;
    }
    public boolean isDisabled(final int level){
        return this.thresholdInt>level;
    }
    public void overrideAsNeeded(final String override){
        LogLog.warn("The Hiearchy.overrideAsNeeded method has been deprecated.");
    }
    public void resetConfiguration(){
        this.getRootLogger().setLevel(Level.DEBUG);
        this.root.setResourceBundle(null);
        this.setThreshold(Level.ALL);
        synchronized(this.ht){
            this.shutdown();
            final Enumeration cats=this.getCurrentLoggers();
            while(cats.hasMoreElements()){
                final Logger c=cats.nextElement();
                c.setLevel(null);
                c.setAdditivity(true);
                c.setResourceBundle(null);
            }
        }
        this.rendererMap.clear();
    }
    public void setDisableOverride(final String override){
        LogLog.warn("The Hiearchy.setDisableOverride method has been deprecated.");
    }
    public void setRenderer(final Class renderedClass,final ObjectRenderer renderer){
        this.rendererMap.put(renderedClass,renderer);
    }
    public void shutdown(){
        final Logger root=this.getRootLogger();
        root.closeNestedAppenders();
        synchronized(this.ht){
            Enumeration cats=this.getCurrentLoggers();
            while(cats.hasMoreElements()){
                final Logger c=cats.nextElement();
                c.closeNestedAppenders();
            }
            root.removeAllAppenders();
            cats=this.getCurrentLoggers();
            while(cats.hasMoreElements()){
                final Logger c=cats.nextElement();
                c.removeAllAppenders();
            }
        }
    }
    private final void updateParents(final Logger cat){
        final String name=cat.name;
        final int length=name.length();
        boolean parentFound=false;
        for(int i=name.lastIndexOf(46,length-1);i>=0;i=name.lastIndexOf(46,i-1)){
            final String substr=name.substring(0,i);
            final CategoryKey key=new CategoryKey(substr);
            final Object o=this.ht.get(key);
            if(o==null){
                final ProvisionNode pn=new ProvisionNode(cat);
                this.ht.put(key,pn);
            }
            else{
                if(o instanceof Category){
                    parentFound=true;
                    cat.parent=(Category)o;
                    break;
                }
                if(o instanceof ProvisionNode){
                    ((ProvisionNode)o).addElement(cat);
                }
                else{
                    final Exception e=new IllegalStateException("unexpected object type "+o.getClass()+" in ht.");
                    e.printStackTrace();
                }
            }
        }
        if(!parentFound){
            cat.parent=this.root;
        }
    }
    private final void updateChildren(final ProvisionNode pn,final Logger logger){
        for(int last=pn.size(),i=0;i<last;++i){
            final Logger l=pn.elementAt(i);
            if(!l.parent.name.startsWith(logger.name)){
                logger.parent=l.parent;
                l.parent=logger;
            }
        }
    }
}
