package org.apache.wicket.util.resource.locator;

import java.util.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;

public class ResourceNameIterator implements Iterator<String>{
    private final String path;
    private final String extensions;
    private final Locale locale;
    private final boolean strict;
    private final StyleAndVariationResourceNameIterator styleIterator;
    private LocaleResourceNameIterator localeIterator;
    private ExtensionResourceNameIterator extensionsIterator;
    public ResourceNameIterator(final String path,final String style,final String variation,final Locale locale,final String extensions,final boolean strict){
        super();
        this.locale=locale;
        if(extensions==null&&path!=null&&path.indexOf(46)!=-1){
            this.extensions=Strings.afterLast(path,'.');
            this.path=Strings.beforeLast(path,'.');
        }
        else{
            this.extensions=extensions;
            this.path=path;
        }
        this.styleIterator=this.newStyleAndVariationResourceNameIterator(style,variation);
        this.strict=strict;
    }
    public Locale getLocale(){
        return this.localeIterator.getLocale();
    }
    public String getStyle(){
        return this.styleIterator.getStyle();
    }
    public String getVariation(){
        return this.styleIterator.getVariation();
    }
    public String getExtension(){
        return this.extensionsIterator.getExtension();
    }
    public boolean hasNext(){
        if(this.extensionsIterator!=null){
            if(this.extensionsIterator.hasNext()){
                return true;
            }
            this.extensionsIterator=null;
        }
        if(this.localeIterator!=null){
            while(this.localeIterator.hasNext()){
                this.localeIterator.next();
                this.extensionsIterator=this.newExtensionResourceNameIterator(this.extensions);
                if(this.extensionsIterator.hasNext()){
                    return true;
                }
            }
            this.localeIterator=null;
        }
        while(this.styleIterator.hasNext()){
            this.styleIterator.next();
            this.localeIterator=this.newLocaleResourceNameIterator(this.locale,this.strict);
            while(this.localeIterator.hasNext()){
                this.localeIterator.next();
                this.extensionsIterator=this.newExtensionResourceNameIterator(this.extensions);
                if(this.extensionsIterator.hasNext()){
                    return true;
                }
            }
            if(this.strict){
                break;
            }
        }
        return false;
    }
    public String next(){
        if(this.extensionsIterator!=null){
            this.extensionsIterator.next();
            return this.toString();
        }
        throw new WicketRuntimeException("Illegal call of next(). Iterator not properly initialized");
    }
    public String toString(){
        return this.path+this.prepend(this.getVariation(),'_')+this.prepend(this.getStyle(),'_')+this.prepend(this.getLocale(),'_')+this.prepend(this.getExtension(),'.');
    }
    private String prepend(final Object string,final char prepend){
        return (string!=null)?(prepend+string.toString()):"";
    }
    protected LocaleResourceNameIterator newLocaleResourceNameIterator(final Locale locale,final boolean strict){
        return new LocaleResourceNameIterator(locale,strict);
    }
    protected StyleAndVariationResourceNameIterator newStyleAndVariationResourceNameIterator(final String style,final String variation){
        return new StyleAndVariationResourceNameIterator(style,variation);
    }
    protected ExtensionResourceNameIterator newExtensionResourceNameIterator(final String extensions){
        return new ExtensionResourceNameIterator(extensions,',');
    }
    public void remove(){
    }
}
