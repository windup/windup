package org.apache.wicket.request.mapper;

import org.apache.wicket.util.string.*;
import org.apache.wicket.request.resource.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.lang.*;

public abstract class AbstractResourceReferenceMapper extends AbstractComponentMapper{
    public static CharSequence escapeAttributesSeparator(final String attribute){
        final CharSequence tmp=Strings.replaceAll((CharSequence)attribute,(CharSequence)"~",(CharSequence)"~~");
        return Strings.replaceAll(tmp,(CharSequence)"-",(CharSequence)"~");
    }
    public static String unescapeAttributesSeparator(final String attribute){
        final String tmp=attribute.replaceAll("(\\w)~(\\w)","$1-$2");
        return Strings.replaceAll((CharSequence)tmp,(CharSequence)"~~",(CharSequence)"~").toString();
    }
    public static String encodeResourceReferenceAttributes(final ResourceReference.UrlAttributes attributes){
        if(attributes==null||(attributes.getLocale()==null&&attributes.getStyle()==null&&attributes.getVariation()==null)){
            return null;
        }
        final StringBuilder res=new StringBuilder(32);
        if(attributes.getLocale()!=null){
            res.append(attributes.getLocale());
        }
        final boolean styleEmpty=Strings.isEmpty((CharSequence)attributes.getStyle());
        if(!styleEmpty){
            res.append('-');
            res.append(escapeAttributesSeparator(attributes.getStyle()));
        }
        if(!Strings.isEmpty((CharSequence)attributes.getVariation())){
            if(styleEmpty){
                res.append("--");
            }
            else{
                res.append('-');
            }
            res.append(escapeAttributesSeparator(attributes.getVariation()));
        }
        return res.toString();
    }
    private static String nonEmpty(final String s){
        if(Strings.isEmpty((CharSequence)s)){
            return null;
        }
        return s;
    }
    public static ResourceReference.UrlAttributes decodeResourceReferenceAttributes(final String attributes){
        Locale locale=null;
        String style=null;
        String variation=null;
        if(!Strings.isEmpty((CharSequence)attributes)){
            final String[] split=Strings.split(attributes,'-');
            locale=parseLocale(split[0]);
            if(split.length==2){
                style=nonEmpty(unescapeAttributesSeparator(split[1]));
            }
            else if(split.length==3){
                style=nonEmpty(unescapeAttributesSeparator(split[1]));
                variation=nonEmpty(unescapeAttributesSeparator(split[2]));
            }
        }
        return new ResourceReference.UrlAttributes(locale,style,variation);
    }
    private static Locale parseLocale(final String locale){
        if(Strings.isEmpty((CharSequence)locale)){
            return null;
        }
        final String[] parts=locale.toLowerCase().split("_",3);
        if(parts.length==1){
            return new Locale(parts[0]);
        }
        if(parts.length==2){
            return new Locale(parts[0],parts[1]);
        }
        if(parts.length==3){
            return new Locale(parts[0],parts[1],parts[2]);
        }
        return null;
    }
    protected void encodeResourceReferenceAttributes(final Url url,final ResourceReference reference){
        final String encoded=encodeResourceReferenceAttributes(reference.getUrlAttributes());
        if(!Strings.isEmpty((CharSequence)encoded)){
            url.getQueryParameters().add(new Url.QueryParameter(encoded,""));
        }
    }
    protected ResourceReference.UrlAttributes getResourceReferenceAttributes(final Url url){
        Args.notNull((Object)url,"url");
        if(url.getQueryParameters().size()>0){
            final Url.QueryParameter param=(Url.QueryParameter)url.getQueryParameters().get(0);
            if(Strings.isEmpty((CharSequence)param.getValue())){
                return decodeResourceReferenceAttributes(param.getName());
            }
        }
        return new ResourceReference.UrlAttributes(null,null,null);
    }
    protected void removeMetaParameter(final Url urlCopy){
        urlCopy.getQueryParameters().remove(0);
    }
}
