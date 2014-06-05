package org.apache.wicket.request.resource;

import java.io.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public abstract class ResourceReference implements Serializable{
    private static final long serialVersionUID=1L;
    private final Key data;
    public ResourceReference(final Key key){
        super();
        Args.notNull((Object)key,"key");
        this.data=key;
    }
    public ResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super();
        Args.notNull((Object)scope,"scope");
        Args.notNull((Object)name,"name");
        this.data=new Key(scope.getName(),name,locale,style,variation);
    }
    public ResourceReference(final Class<?> scope,final String name){
        this(scope,name,null,null,null);
    }
    public ResourceReference(final String name){
        this((Class<?>)Application.class,name,null,null,null);
    }
    public final Key getKey(){
        return this.data;
    }
    public String getName(){
        return this.data.getName();
    }
    public final String getExtension(){
        String name=this.getName();
        final int queryAt=name.indexOf(63);
        if(queryAt!=-1){
            name=name.substring(0,queryAt);
        }
        final int extPos=name.lastIndexOf(46);
        if(extPos==-1){
            return null;
        }
        return name.substring(extPos+1).toLowerCase();
    }
    public Class<?> getScope(){
        return WicketObjects.resolveClass(this.data.getScope());
    }
    public Locale getLocale(){
        return this.data.getLocale();
    }
    public String getStyle(){
        return this.data.getStyle();
    }
    public String getVariation(){
        return this.data.getVariation();
    }
    public boolean canBeRegistered(){
        return true;
    }
    public boolean equals(final Object obj){
        if(this==obj){
            return true;
        }
        if(!(obj instanceof ResourceReference)){
            return false;
        }
        final ResourceReference that=(ResourceReference)obj;
        return Objects.equal((Object)this.data,(Object)that.data);
    }
    public int hashCode(){
        return this.data.hashCode();
    }
    public abstract IResource getResource();
    public UrlAttributes getUrlAttributes(){
        return new UrlAttributes(this.getLocale(),this.getStyle(),this.getVariation());
    }
    public String toString(){
        return this.data.toString();
    }
    public static class UrlAttributes{
        private final Locale locale;
        private final String style;
        private final String variation;
        public UrlAttributes(final Locale locale,final String style,final String variation){
            super();
            this.locale=locale;
            this.style=style;
            this.variation=variation;
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
        public boolean equals(final Object obj){
            if(this==obj){
                return true;
            }
            if(!(obj instanceof UrlAttributes)){
                return false;
            }
            final UrlAttributes that=(UrlAttributes)obj;
            return Objects.equal((Object)this.getLocale(),(Object)that.getLocale())&&Objects.equal((Object)this.getStyle(),(Object)that.getStyle())&&Objects.equal((Object)this.getVariation(),(Object)that.getVariation());
        }
        public int hashCode(){
            return Objects.hashCode(new Object[] { this.getLocale(),this.getStyle(),this.getVariation() });
        }
        public String toString(){
            return "locale: "+this.locale+"; style: "+this.style+"; variation: "+this.variation;
        }
    }
    public static final class Key implements Serializable{
        private static final long serialVersionUID=1L;
        final String scope;
        final String name;
        final Locale locale;
        final String style;
        final String variation;
        public Key(final ResourceReference reference){
            this(reference.getScope().getName(),reference.getName(),reference.getLocale(),reference.getStyle(),reference.getVariation());
        }
        public Key(final String scope,final String name,final Locale locale,final String style,final String variation){
            super();
            Args.notNull((Object)scope,"scope");
            Args.notNull((Object)name,"name");
            this.scope=scope.intern();
            this.name=name.intern();
            this.locale=locale;
            this.style=((style!=null)?style.intern():null);
            this.variation=((variation!=null)?variation.intern():null);
        }
        public boolean equals(final Object obj){
            if(this==obj){
                return true;
            }
            if(!(obj instanceof Key)){
                return false;
            }
            final Key that=(Key)obj;
            return Objects.equal((Object)this.scope,(Object)that.scope)&&Objects.equal((Object)this.name,(Object)that.name)&&Objects.equal((Object)this.locale,(Object)that.locale)&&Objects.equal((Object)this.style,(Object)that.style)&&Objects.equal((Object)this.variation,(Object)that.variation);
        }
        public int hashCode(){
            return Objects.hashCode(new Object[] { this.scope,this.name,this.locale,this.style,this.variation });
        }
        public final String getScope(){
            return this.scope;
        }
        public final Class<?> getScopeClass(){
            return WicketObjects.resolveClass(this.scope);
        }
        public final String getName(){
            return this.name;
        }
        public final Locale getLocale(){
            return this.locale;
        }
        public final String getStyle(){
            return this.style;
        }
        public final String getVariation(){
            return this.variation;
        }
        public String toString(){
            return "scope: "+this.scope+"; name: "+this.name+"; locale: "+this.locale+"; style: "+this.style+"; variation: "+this.variation;
        }
    }
}
