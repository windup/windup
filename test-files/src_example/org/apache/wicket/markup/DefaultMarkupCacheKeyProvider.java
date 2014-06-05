package org.apache.wicket.markup;

import org.apache.wicket.*;
import java.util.*;

public class DefaultMarkupCacheKeyProvider implements IMarkupCacheKeyProvider{
    public String getCacheKey(final MarkupContainer container,final Class<?> clazz){
        final String classname=clazz.getName();
        final StringBuilder buffer=new StringBuilder(classname.length()+64);
        buffer.append(classname);
        if(container.getVariation()!=null){
            buffer.append('_').append(container.getVariation());
        }
        if(container.getStyle()!=null){
            buffer.append('_').append(container.getStyle());
        }
        final Locale locale=container.getLocale();
        if(locale!=null){
            buffer.append('_').append(locale.getLanguage());
            final boolean hasLocale=locale.getLanguage().length()!=0;
            final boolean hasCountry=locale.getCountry().length()!=0;
            final boolean hasVariant=locale.getVariant().length()!=0;
            if(hasCountry||(hasLocale&&hasVariant)){
                buffer.append('_').append(locale.getCountry());
            }
            if(hasVariant&&(hasLocale||hasCountry)){
                buffer.append('_').append(locale.getVariant());
            }
        }
        buffer.append('.').append(container.getMarkupType().getExtension());
        return buffer.toString();
    }
}
