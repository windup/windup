package org.apache.wicket;

import org.apache.wicket.model.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.resource.loader.*;
import java.util.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.util.string.interpolator.*;
import org.apache.wicket.util.convert.*;
import org.slf4j.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.apache.wicket.util.lang.*;

public class Localizer{
    private static final Logger log;
    private static final String NULL_VALUE="<null-value>";
    private Map<String,String> cache;
    private final ClassMetaDatabase metaDatabase;
    public static Localizer get(){
        return Application.get().getResourceSettings().getLocalizer();
    }
    public Localizer(){
        super();
        this.cache=this.newCache();
        this.metaDatabase=new ClassMetaDatabase();
    }
    public final void clearCache(){
        if(this.cache!=null){
            this.cache=this.newCache();
        }
    }
    public String getString(final String key,final Component component) throws MissingResourceException{
        return this.getString(key,component,null,null,null,null);
    }
    public String getString(final String key,final Component component,final IModel<?> model) throws MissingResourceException{
        return this.getString(key,component,model,null,null,null);
    }
    public String getString(final String key,final Component component,final String defaultValue) throws MissingResourceException{
        return this.getString(key,component,null,null,null,defaultValue);
    }
    public String getString(final String key,final Component component,final IModel<?> model,final String defaultValue) throws MissingResourceException{
        return this.getString(key,component,model,null,null,defaultValue);
    }
    public String getString(final String key,final Component component,final IModel<?> model,final Locale locale,final String style,final String defaultValue) throws MissingResourceException{
        final IResourceSettings resourceSettings=Application.get().getResourceSettings();
        String value=this.getStringIgnoreSettings(key,component,model,locale,style,null);
        if(value==null&&defaultValue!=null&&resourceSettings.getUseDefaultOnMissingResource()){
            value=defaultValue;
            if(value!=null){
                return this.substitutePropertyExpressions(component,value,model);
            }
        }
        if(value!=null){
            return value;
        }
        if(resourceSettings.getThrowExceptionOnMissingResource()){
            final AppendingStringBuffer message=new AppendingStringBuffer((CharSequence)"Unable to find property: '");
            message.append(key);
            message.append("'");
            if(component!=null){
                message.append(" for component: ");
                message.append(component.getPageRelativePath());
                message.append(" [class=").append(component.getClass().getName()).append("]");
            }
            message.append(". Locale: ").append((Object)locale).append(", style: ").append(style);
            throw new MissingResourceException(message.toString(),(component!=null)?component.getClass().getName():"",key);
        }
        return "[Warning: Property for '"+key+"' not found]";
    }
    public final String getStringIgnoreSettings(final String key,final Component component,final IModel<?> model,final String defaultValue){
        return this.getStringIgnoreSettings(key,component,model,null,null,defaultValue);
    }
    public final String getStringIgnoreSettings(final String key,final Component component,final IModel<?> model,Locale locale,String style,final String defaultValue){
        boolean addedToPage=false;
        if(component!=null){
            if(component instanceof Page||null!=component.findParent((Class<Page>)Page.class)){
                addedToPage=true;
            }
            if(!addedToPage&&Localizer.log.isWarnEnabled()){
                Localizer.log.warn("Tried to retrieve a localized string for a component that has not yet been added to the page. This can sometimes lead to an invalid or no localized resource returned. Make sure you are not calling Component#getString() inside your Component's constructor. Offending component: {}",component);
            }
        }
        String cacheKey=null;
        String value=null;
        final String variation=(component!=null)?component.getVariation():null;
        if(locale==null&&component!=null){
            locale=component.getLocale();
        }
        if(locale==null){
            locale=(Session.exists()?Session.get().getLocale():Locale.getDefault());
        }
        if(style==null&&component!=null){
            style=component.getStyle();
        }
        if(style==null){
            style=(Session.exists()?Session.get().getStyle():null);
        }
        if(this.cache!=null&&(component==null||addedToPage)){
            cacheKey=this.getCacheKey(key,component,locale,style,variation);
        }
        if(cacheKey!=null&&this.cache.containsKey(cacheKey)){
            value=this.getFromCache(cacheKey);
            if(Localizer.log.isDebugEnabled()){
                Localizer.log.debug("Property found in cache: '"+key+"'; Component: '"+((component!=null)?component.toString(false):null)+"'; value: '"+value+'\'');
            }
        }
        else{
            if(Localizer.log.isDebugEnabled()){
                Localizer.log.debug("Locate property: key: '"+key+"'; Component: '"+((component!=null)?component.toString(false):null)+'\'');
            }
            Iterator<IStringResourceLoader> iter;
            IStringResourceLoader loader;
            for(iter=(Iterator<IStringResourceLoader>)this.getStringResourceLoaders().iterator(),value=null;iter.hasNext()&&value==null;value=loader.loadStringResource(component,key,locale,style,variation)){
                loader=(IStringResourceLoader)iter.next();
            }
            if(cacheKey!=null){
                this.putIntoCache(cacheKey,value);
            }
            if(value==null&&Localizer.log.isDebugEnabled()){
                Localizer.log.debug("Property not found; key: '"+key+"'; Component: '"+((component!=null)?component.toString(false):null)+'\'');
            }
        }
        if(value==null){
            value=defaultValue;
        }
        if(value!=null){
            return this.substitutePropertyExpressions(component,value,model);
        }
        return null;
    }
    protected List<IStringResourceLoader> getStringResourceLoaders(){
        return Application.get().getResourceSettings().getStringResourceLoaders();
    }
    protected void putIntoCache(final String cacheKey,final String string){
        if(this.cache==null){
            return;
        }
        if(string==null){
            this.cache.put(cacheKey,"<null-value>");
        }
        else{
            this.cache.put(cacheKey,string);
        }
    }
    protected String getFromCache(final String cacheKey){
        if(this.cache==null){
            return null;
        }
        final String value=(String)this.cache.get(cacheKey);
        if("<null-value>"==value){
            return null;
        }
        return value;
    }
    protected String getCacheKey(final String key,final Component component,final Locale locale,final String style,final String variation){
        if(component!=null){
            final StringBuilder buffer=new StringBuilder(200);
            buffer.append(key);
            Component parent;
            for(Component cursor=component;cursor!=null;cursor=parent){
                buffer.append('-').append(this.metaDatabase.id((Class<?>)cursor.getClass()));
                if(cursor instanceof Page){
                    break;
                }
                parent=cursor.getParent();
                final boolean skip=parent instanceof AbstractRepeater;
                if(!skip){
                    final String cursorKey=cursor.isAuto()?"wicket-auto":cursor.getId();
                    buffer.append(':').append(cursorKey);
                }
            }
            buffer.append('-').append(locale);
            buffer.append('-').append(style);
            buffer.append('-').append(variation);
            return buffer.toString();
        }
        return key+'-'+locale.toString()+'-'+style;
    }
    public String substitutePropertyExpressions(final Component component,final String string,final IModel<?> model){
        if(string!=null&&model!=null){
            return new PropertyVariableInterpolator(string,model.getObject()){
                protected String toString(final Object value){
                    IConverter converter;
                    Locale locale;
                    if(component==null){
                        converter=Application.get().getConverterLocator().getConverter((Class<Object>)value.getClass());
                        if(Session.exists()){
                            locale=Session.get().getLocale();
                        }
                        else{
                            locale=Locale.getDefault();
                        }
                    }
                    else{
                        converter=component.getConverter((Class<Object>)value.getClass());
                        locale=component.getLocale();
                    }
                    return converter.convertToString(value,locale);
                }
            }.toString();
        }
        return string;
    }
    public final void setEnableCache(final boolean value){
        if(!value){
            this.cache=null;
        }
        else if(this.cache==null){
            this.cache=this.newCache();
        }
    }
    protected Map<String,String> newCache(){
        return (Map<String,String>)new ConcurrentHashMap();
    }
    static{
        log=LoggerFactory.getLogger(Localizer.class);
    }
    private static class ClassMetaDatabase{
        private final ConcurrentMap<String,Long> nameToId;
        private final AtomicLong nameCounter;
        private ClassMetaDatabase(){
            super();
            this.nameToId=(ConcurrentMap<String,Long>)Generics.newConcurrentHashMap();
            this.nameCounter=new AtomicLong();
        }
        public long id(final Class<?> clazz){
            final String name=clazz.getName();
            Long id=(Long)this.nameToId.get(name);
            if(id==null){
                id=this.nameCounter.incrementAndGet();
                final Long previousId=this.nameToId.putIfAbsent(name,id);
                if(previousId!=null){
                    id=previousId;
                }
            }
            return id;
        }
    }
}
