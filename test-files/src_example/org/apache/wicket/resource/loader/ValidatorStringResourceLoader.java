package org.apache.wicket.resource.loader;

import org.apache.wicket.*;
import org.apache.wicket.markup.html.form.*;
import java.util.*;
import org.apache.wicket.validation.*;
import org.slf4j.*;

public class ValidatorStringResourceLoader extends ComponentStringResourceLoader{
    private static final Logger log;
    public String loadStringResource(final Class<?> clazz,final String key,final Locale locale,final String style,final String variation){
        if(clazz==null||!IValidator.class.isAssignableFrom(clazz)){
            return null;
        }
        return super.loadStringResource(clazz,key,locale,style,variation);
    }
    public String loadStringResource(final Component component,final String key,final Locale locale,final String style,final String variation){
        if(component==null||!(component instanceof FormComponent)){
            return null;
        }
        final FormComponent<?> fc=(FormComponent<?>)component;
        for(final IValidator<?> validator : fc.getValidators()){
            final Class<?> scope=this.getScope(validator);
            final String resource=this.loadStringResource(scope,key,locale,style,variation);
            if(resource!=null){
                return resource;
            }
        }
        return null;
    }
    private Class<? extends IValidator> getScope(final IValidator<?> validator){
        Class<? extends IValidator> scope;
        if(validator instanceof ValidatorAdapter){
            scope=(Class<? extends IValidator>)((ValidatorAdapter)validator).getValidator().getClass();
        }
        else{
            scope=(Class<? extends IValidator>)validator.getClass();
        }
        return scope;
    }
    static{
        log=LoggerFactory.getLogger(ValidatorStringResourceLoader.class);
    }
}
