package org.apache.wicket.resource.loader;

import java.util.*;
import org.apache.wicket.*;

public class BundleStringResourceLoader implements IStringResourceLoader{
    private final String bundleName;
    public BundleStringResourceLoader(final String bundleName){
        super();
        this.bundleName=bundleName;
    }
    public final String loadStringResource(final Class<?> clazz,final String key,Locale locale,final String style,final String variation){
        if(locale==null){
            locale=(Session.exists()?Session.get().getLocale():Locale.getDefault());
        }
        try{
            return ResourceBundle.getBundle(this.bundleName,locale).getString(key);
        }
        catch(MissingResourceException e){
            return null;
        }
    }
    public final String loadStringResource(final Component component,final String key,final Locale locale,final String style,final String variation){
        return this.loadStringResource((Class<?>)null,key,locale,style,variation);
    }
}
