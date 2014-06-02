package org.apache.wicket.markup;

import java.lang.ref.*;
import java.util.*;
import org.apache.wicket.*;

public class ContainerInfo{
    private final WeakReference<Class<?>> containerClassRef;
    private final Locale locale;
    private final String style;
    private final String variation;
    private final MarkupType markupType;
    public ContainerInfo(final MarkupContainer container){
        this((Class<?>)container.getClass(),container.getLocale(),container.getStyle(),container.getVariation(),container.getMarkupType());
    }
    public ContainerInfo(final Class<?> containerClass,final Locale locale,final String style,final String variation,final MarkupType markupType){
        super();
        this.containerClassRef=(WeakReference<Class<?>>)new WeakReference(containerClass);
        this.locale=locale;
        this.style=style;
        this.variation=variation;
        this.markupType=markupType;
    }
    public Class<?> getContainerClass(){
        return (Class<?>)this.containerClassRef.get();
    }
    public String getFileExtension(){
        return (this.markupType!=null)?this.markupType.getExtension():null;
    }
    public Locale getLocale(){
        return this.locale;
    }
    public String getStyle(){
        return this.style;
    }
    public String getVariation(){
        return this.variation;
    }
    public String toString(){
        final Class<?> classRef=(Class<?>)this.containerClassRef.get();
        return ((classRef!=null)?classRef.getName():"null class")+":"+this.locale+":"+this.style+":"+this.markupType;
    }
}
