package org.apache.wicket.resource.loader;

import java.util.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class InitializerStringResourceLoader extends ComponentStringResourceLoader{
    private static final Logger log;
    private List<IInitializer> initializers;
    public InitializerStringResourceLoader(final List<IInitializer> initializers){
        super();
        this.initializers=initializers;
    }
    public String loadStringResource(final Class<?> clazz,final String key,final Locale locale,final String style,final String variation){
        for(final IInitializer initializer : this.initializers){
            final String string=super.loadStringResource((Class<?>)initializer.getClass(),key,locale,style,variation);
            if(string!=null){
                return string;
            }
        }
        return null;
    }
    public String loadStringResource(final Component component,final String key,final Locale locale,final String style,final String variation){
        return this.loadStringResource((Class<?>)null,key,locale,style,variation);
    }
    static{
        log=LoggerFactory.getLogger(InitializerStringResourceLoader.class);
    }
}
