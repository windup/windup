package org.apache.wicket.util.resource.locator;

import java.util.*;
import org.apache.wicket.util.string.*;

public class LocaleResourceNameIterator implements Iterator<String>{
    private final Locale locale;
    private int state;
    private final boolean strict;
    public LocaleResourceNameIterator(final Locale locale,final boolean strict){
        super();
        this.state=0;
        this.locale=locale;
        this.strict=strict;
    }
    public Locale getLocale(){
        if(this.state==1){
            return this.locale;
        }
        if(this.state==2){
            return new Locale(this.locale.getLanguage(),this.locale.getCountry());
        }
        if(this.state==3){
            return new Locale(this.locale.getLanguage());
        }
        return null;
    }
    public boolean hasNext(){
        int limit=4;
        if(this.strict&&this.locale!=null){
            limit=3;
        }
        return this.state<limit;
    }
    public String next(){
        if(this.locale==null){
            this.state=999;
            return "";
        }
        final String language=this.locale.getLanguage();
        final String country=this.locale.getCountry();
        final String variant=this.locale.getVariant();
        if(this.state==0){
            ++this.state;
            if(!Strings.isEmpty((CharSequence)variant)){
                return '_'+language+'_'+country+'_'+variant;
            }
        }
        if(this.state==1){
            ++this.state;
            if(!Strings.isEmpty((CharSequence)country)){
                return '_'+language+'_'+country;
            }
        }
        if(this.state==2){
            ++this.state;
            if(!Strings.isEmpty((CharSequence)language)){
                return '_'+language;
            }
        }
        ++this.state;
        return "";
    }
    public void remove(){
    }
}
