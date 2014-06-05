package org.apache.wicket.protocol.http;

import org.apache.wicket.*;

public class ContextParamWebApplicationFactory implements IWebApplicationFactory{
    public static final String APP_CLASS_PARAM="applicationClassName";
    public WebApplication createApplication(final WicketFilter filter){
        final String applicationClassName=filter.getFilterConfig().getInitParameter("applicationClassName");
        if(applicationClassName==null){
            throw new WicketRuntimeException("servlet init param [applicationClassName] is missing. If you are trying to use your own implementation of IWebApplicationFactory and get this message then the servlet init param [applicationFactoryClassName] is missing");
        }
        return this.createApplication(applicationClassName);
    }
    protected WebApplication createApplication(final String applicationClassName){
        try{
            ClassLoader loader=Thread.currentThread().getContextClassLoader();
            if(loader==null){
                loader=this.getClass().getClassLoader();
            }
            final Class<?> applicationClass=(Class<?>)Class.forName(applicationClassName,false,loader);
            if(WebApplication.class.isAssignableFrom(applicationClass)){
                return (WebApplication)applicationClass.newInstance();
            }
            throw new WicketRuntimeException("Application class "+applicationClassName+" must be a subclass of WebApplication");
        }
        catch(ClassNotFoundException e){
            throw new WicketRuntimeException("Unable to create application of class "+applicationClassName,e);
        }
        catch(InstantiationException e2){
            throw new WicketRuntimeException("Unable to create application of class "+applicationClassName,e2);
        }
        catch(IllegalAccessException e3){
            throw new WicketRuntimeException("Unable to create application of class "+applicationClassName,e3);
        }
        catch(SecurityException e4){
            throw new WicketRuntimeException("Unable to create application of class "+applicationClassName,e4);
        }
    }
    public void destroy(final WicketFilter filter){
    }
}
