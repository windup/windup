package org.apache.wicket.resource.loader;

import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.resource.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.slf4j.*;
import java.io.*;

public class ComponentStringResourceLoader implements IStringResourceLoader{
    private static final Logger log;
    public String loadStringResource(Class<?> clazz,final String key,final Locale locale,final String style,final String variation){
        if(clazz==null){
            return null;
        }
        if(ComponentStringResourceLoader.log.isDebugEnabled()){
            ComponentStringResourceLoader.log.debug("key: '"+key+"'; class: '"+clazz.getName()+"'; locale: '"+locale+"'; Style: '"+style+"'; Variation: '"+variation+'\'');
        }
        final IPropertiesFactory propertiesFactory=this.getPropertiesFactory();
        while(true){
            final String path=clazz.getName().replace('.','/');
            final ResourceNameIterator iter=this.newResourceNameIterator(path,locale,style,variation);
            while(iter.hasNext()){
                final String newPath=iter.next();
                final Properties props=propertiesFactory.load(clazz,newPath);
                if(props!=null){
                    final String value=props.getString(key);
                    if(value!=null){
                        return value;
                    }
                    continue;
                }
            }
            if(this.isStopResourceSearch(clazz)){
                break;
            }
            clazz=(Class<?>)clazz.getSuperclass();
            if(clazz==null){
                break;
            }
        }
        return null;
    }
    protected ResourceNameIterator newResourceNameIterator(final String path,final Locale locale,final String style,final String variation){
        return Application.get().getResourceSettings().getResourceStreamLocator().newResourceNameIterator(path,locale,style,variation,null,false);
    }
    protected IPropertiesFactory getPropertiesFactory(){
        return Application.get().getResourceSettings().getPropertiesFactory();
    }
    public String loadStringResource(final Component component,final String key,final Locale locale,final String style,final String variation){
        if(component==null){
            return null;
        }
        if(ComponentStringResourceLoader.log.isDebugEnabled()){
            ComponentStringResourceLoader.log.debug("component: '"+component.toString(false)+"'; key: '"+key+'\'');
        }
        String string=null;
        String prefix=this.getResourcePath(component);
        for(final Component current : this.getComponentTrail(component)){
            final Class<?> clazz=(Class<?>)current.getClass();
            if(!Strings.isEmpty((CharSequence)prefix)){
                string=this.loadStringResource(clazz,prefix+'.'+key,locale,style,variation);
                if(string!=null){
                    return string;
                }
                if(!(current instanceof AbstractRepeater)){
                    prefix=Strings.afterFirst(prefix,'.');
                }
            }
            string=this.loadStringResource(clazz,key,locale,style,variation);
            if(string!=null){
                return string;
            }
        }
        return string;
    }
    protected String getResourcePath(final Component component){
        Component current=(Component)Args.notNull((Object)component,"component");
        final StringBuilder buffer=new StringBuilder();
        while(current.getParent()!=null){
            final boolean skip=current.getParent() instanceof AbstractRepeater;
            if(!skip){
                if(buffer.length()>0){
                    buffer.insert(0,'.');
                }
                buffer.insert(0,current.getId());
            }
            current=current.getParent();
        }
        return buffer.toString();
    }
    private List<Component> getComponentTrail(Component component){
        final List<Component> path=(List<Component>)new ArrayList();
        while(component!=null){
            path.add(0,component);
            component=component.getParent();
        }
        return path;
    }
    protected boolean isStopResourceSearch(final Class<?> clazz){
        return clazz==null||clazz.equals(Object.class)||clazz.equals(Application.class)||(clazz.equals(WebPage.class)||clazz.equals(WebMarkupContainer.class)||clazz.equals(WebComponent.class))||clazz.equals(Page.class)||clazz.equals(MarkupContainer.class)||clazz.equals(Component.class);
    }
    static{
        log=LoggerFactory.getLogger(ComponentStringResourceLoader.class);
    }
}
