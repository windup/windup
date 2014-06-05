package org.apache.wicket;

import java.math.*;
import java.sql.*;
import org.apache.wicket.util.convert.converter.*;
import java.lang.ref.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.convert.*;

public class ConverterLocator implements IConverterLocator{
    private static final long serialVersionUID=1L;
    private final Map<String,IConverter<?>> classToConverter;
    public ConverterLocator(){
        super();
        this.classToConverter=(Map<String,IConverter<?>>)new HashMap();
        this.set((Class<?>)Boolean.TYPE,(IConverter<?>)BooleanConverter.INSTANCE);
        this.set((Class<?>)Boolean.class,(IConverter<?>)BooleanConverter.INSTANCE);
        this.set((Class<?>)Byte.TYPE,(IConverter<?>)ByteConverter.INSTANCE);
        this.set((Class<?>)Byte.class,(IConverter<?>)ByteConverter.INSTANCE);
        this.set((Class<?>)Character.TYPE,(IConverter<?>)CharacterConverter.INSTANCE);
        this.set((Class<?>)Character.class,(IConverter<?>)CharacterConverter.INSTANCE);
        this.set((Class<?>)Double.TYPE,(IConverter<?>)DoubleConverter.INSTANCE);
        this.set((Class<?>)Double.class,(IConverter<?>)DoubleConverter.INSTANCE);
        this.set((Class<?>)Float.TYPE,(IConverter<?>)FloatConverter.INSTANCE);
        this.set((Class<?>)Float.class,(IConverter<?>)FloatConverter.INSTANCE);
        this.set((Class<?>)Integer.TYPE,(IConverter<?>)IntegerConverter.INSTANCE);
        this.set((Class<?>)Integer.class,(IConverter<?>)IntegerConverter.INSTANCE);
        this.set((Class<?>)Long.TYPE,(IConverter<?>)LongConverter.INSTANCE);
        this.set((Class<?>)Long.class,(IConverter<?>)LongConverter.INSTANCE);
        this.set((Class<?>)Short.TYPE,(IConverter<?>)ShortConverter.INSTANCE);
        this.set((Class<?>)Short.class,(IConverter<?>)ShortConverter.INSTANCE);
        this.set((Class<?>)BigDecimal.class,(IConverter<?>)new BigDecimalConverter());
        this.set((Class<?>)Date.class,(IConverter<?>)new DateConverter());
        this.set((Class<?>)java.sql.Date.class,(IConverter<?>)new SqlDateConverter());
        this.set((Class<?>)Time.class,(IConverter<?>)new SqlTimeConverter());
        this.set((Class<?>)Timestamp.class,(IConverter<?>)new SqlTimestampConverter());
        this.set((Class<?>)Calendar.class,(IConverter<?>)new CalendarConverter());
    }
    public final <C> IConverter<C> get(final Class<C> c){
        return (IConverter<C>)this.classToConverter.get(c.getName());
    }
    public final <C> IConverter<C> getConverter(final Class<C> type){
        if(type==null){
            final IConverter<C> converter=(IConverter<C>)new DefaultConverter(String.class);
            return converter;
        }
        final IConverter<C> converter=(IConverter<C>)this.get((Class<Object>)type);
        if(converter==null){
            return (IConverter<C>)new DefaultConverter((Class)type);
        }
        return converter;
    }
    public final IConverter<?> remove(final Class<?> c){
        return (IConverter<?>)this.classToConverter.remove(c.getName());
    }
    public final IConverter<?> set(final Class<?> c,final IConverter<?> converter){
        if(converter==null){
            throw new IllegalArgumentException("CoverterLocator cannot be null");
        }
        if(c==null){
            throw new IllegalArgumentException("Class cannot be null");
        }
        return (IConverter<?>)this.classToConverter.put(c.getName(),converter);
    }
    private static class DefaultConverter<C> implements IConverter<C>{
        private static final long serialVersionUID=1L;
        private final transient WeakReference<Class<C>> type;
        private DefaultConverter(final Class<C> type){
            super();
            this.type=(WeakReference<Class<C>>)new WeakReference(type);
        }
        public C convertToObject(final String value,final Locale locale){
            if(value==null){
                return null;
            }
            final Class<C> theType=(Class<C>)this.type.get();
            if("".equals(value)){
                if(String.class.equals(theType)){
                    return (C)theType.cast((Object)"");
                }
                return null;
            }
            else{
                try{
                    final C converted=(C)Objects.convertValue((Object)value,(Class)theType);
                    if(converted!=null){
                        return converted;
                    }
                    throw new ConversionException("Could not convert value: "+value+" to type: "+theType.getName()+". Could not find compatible converter.").setSourceValue((Object)value);
                }
                catch(Exception e){
                    throw new ConversionException(e.getMessage(),(Throwable)e).setSourceValue((Object)value);
                }
            }
        }
        public String convertToString(final C value,final Locale locale){
            if(value==null||"".equals(value)){
                return "";
            }
            try{
                return (String)Objects.convertValue((Object)value,String.class);
            }
            catch(RuntimeException e){
                throw new ConversionException("Could not convert object of type: "+value.getClass()+" to string. Possible its #toString() returned null. "+"Either install a custom converter (see IConverterLocator) or "+"override #toString() to return a non-null value.",(Throwable)e).setSourceValue((Object)value).setConverter((IConverter)this);
            }
        }
    }
}
