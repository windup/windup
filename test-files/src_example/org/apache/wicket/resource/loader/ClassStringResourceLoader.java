package org.apache.wicket.resource.loader;

import java.lang.ref.*;
import java.util.*;
import org.apache.wicket.*;

public class ClassStringResourceLoader extends ComponentStringResourceLoader{
    private final WeakReference<Class<?>> clazzRef;
    public ClassStringResourceLoader(final Class<?> clazz){
        super();
        if(clazz==null){
            throw new IllegalArgumentException("Parameter 'clazz' must not be null");
        }
        this.clazzRef=(WeakReference<Class<?>>)new WeakReference(clazz);
    }
    public String loadStringResource(final Class<?> clazz,final String key,final Locale locale,final String style,final String variation){
        return super.loadStringResource((Class<?>)this.clazzRef.get(),key,locale,style,variation);
    }
    public String loadStringResource(final Component component,final String key,final Locale locale,final String style,final String variation){
        if(component==null){
            return super.loadStringResource((Class<?>)this.clazzRef.get(),key,locale,style,variation);
        }
        return super.loadStringResource(component,key,locale,style,variation);
    }
}
