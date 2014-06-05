package org.apache.wicket.util.lang;

import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.util.convert.*;

public class PropertyResolverConverter implements IClusterable{
    private static final long serialVersionUID=1L;
    private final IConverterLocator converterSupplier;
    private final Locale locale;
    public PropertyResolverConverter(final IConverterLocator converterSupplier,final Locale locale){
        super();
        this.converterSupplier=converterSupplier;
        this.locale=locale;
    }
    public <T> T convert(final Object object,final Class<T> clz){
        if(object==null){
            return null;
        }
        if(clz.isAssignableFrom(object.getClass())){
            return (T)object;
        }
        final IConverter<T> converter=this.converterSupplier.getConverter(clz);
        if(object instanceof String){
            return (T)converter.convertToObject((String)object,this.locale);
        }
        if(clz==String.class){
            final T result=(T)this.convertToString(object,this.locale);
            return result;
        }
        T result;
        try{
            result=(T)Objects.convertValue(object,(Class)clz);
        }
        catch(RuntimeException ex){
            result=null;
        }
        if(result==null){
            final String tmp=this.convertToString(object,this.locale);
            result=(T)converter.convertToObject(tmp,this.locale);
        }
        return result;
    }
    protected <C> String convertToString(final C object,final Locale locale){
        final Class<C> type=(Class<C>)object.getClass();
        final IConverter<C> converterForObj=this.converterSupplier.getConverter(type);
        return converterForObj.convertToString((Object)object,locale);
    }
}
