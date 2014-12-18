package org.apache.log4j.or;

import org.apache.log4j.or.DefaultRenderer;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.or.ObjectRenderer;
import java.util.Hashtable;

public class RendererMap{
    Hashtable map;
    static ObjectRenderer defaultRenderer;
    static /* synthetic */ Class class$org$apache$log4j$or$ObjectRenderer;
    public RendererMap(){
        super();
        this.map=new Hashtable();
    }
    public static void addRenderer(final RendererSupport repository,final String renderedClassName,final String renderingClassName){
        LogLog.debug("Rendering class: ["+renderingClassName+"], Rendered class: ["+renderedClassName+"].");
        final ObjectRenderer renderer=(ObjectRenderer)OptionConverter.instantiateByClassName(renderingClassName,(RendererMap.class$org$apache$log4j$or$ObjectRenderer==null)?(RendererMap.class$org$apache$log4j$or$ObjectRenderer=class$("org.apache.log4j.or.ObjectRenderer")):RendererMap.class$org$apache$log4j$or$ObjectRenderer,null);
        if(renderer==null){
            LogLog.error("Could not instantiate renderer ["+renderingClassName+"].");
            return;
        }
        try{
            final Class renderedClass=Loader.loadClass(renderedClassName);
            repository.setRenderer(renderedClass,renderer);
        }
        catch(ClassNotFoundException e){
            LogLog.error("Could not find class ["+renderedClassName+"].",e);
        }
    }
    public String findAndRender(final Object o){
        if(o==null){
            return null;
        }
        return this.get(o.getClass()).doRender(o);
    }
    public ObjectRenderer get(final Object o){
        if(o==null){
            return null;
        }
        return this.get(o.getClass());
    }
    public ObjectRenderer get(final Class clazz){
        ObjectRenderer r=null;
        for(Class c=clazz;c!=null;c=c.getSuperclass()){
            r=this.map.get(c);
            if(r!=null){
                return r;
            }
            r=this.searchInterfaces(c);
            if(r!=null){
                return r;
            }
        }
        return RendererMap.defaultRenderer;
    }
    ObjectRenderer searchInterfaces(final Class c){
        ObjectRenderer r=this.map.get(c);
        if(r!=null){
            return r;
        }
        final Class[] ia=c.getInterfaces();
        for(int i=0;i<ia.length;++i){
            r=this.searchInterfaces(ia[i]);
            if(r!=null){
                return r;
            }
        }
        return null;
    }
    public ObjectRenderer getDefaultRenderer(){
        return RendererMap.defaultRenderer;
    }
    public void clear(){
        this.map.clear();
    }
    public void put(final Class clazz,final ObjectRenderer or){
        this.map.put(clazz,or);
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
        RendererMap.defaultRenderer=new DefaultRenderer();
    }
}
