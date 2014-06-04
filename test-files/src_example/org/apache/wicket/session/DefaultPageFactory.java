package org.apache.wicket.session;

import org.apache.wicket.util.lang.*;
import java.util.concurrent.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;
import org.apache.wicket.request.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.markup.*;
import java.lang.reflect.*;
import org.slf4j.*;

public final class DefaultPageFactory implements IPageFactory{
    private static final Logger log;
    private final ConcurrentMap<Class<?>,Constructor<?>> constructorForClass;
    private final ConcurrentMap<String,Boolean> pageToBookmarkableCache;
    public DefaultPageFactory(){
        super();
        this.constructorForClass=(ConcurrentMap<Class<?>,Constructor<?>>)Generics.newConcurrentHashMap();
        this.pageToBookmarkableCache=new ConcurrentHashMap<String,Boolean>();
    }
    public final <C extends IRequestablePage> Page newPage(final Class<C> pageClass){
        try{
            final Constructor<? extends IRequestablePage> constructor=(Constructor<? extends IRequestablePage>)pageClass.getConstructor(null);
            return this.processPage(this.newPage(constructor,null),null);
        }
        catch(NoSuchMethodException e){
            final Constructor<?> constructor2=this.constructor(pageClass,(Class<PageParameters>)PageParameters.class);
            if(constructor2!=null){
                final PageParameters pp=new PageParameters();
                return this.processPage(this.newPage(constructor2,pp),pp);
            }
            throw new WicketRuntimeException("Unable to create page from "+pageClass+". Class does not have a visible default contructor.",e);
        }
    }
    public final <C extends IRequestablePage> Page newPage(final Class<C> pageClass,final PageParameters parameters){
        final Constructor<?> constructor=this.constructor(pageClass,(Class<PageParameters>)PageParameters.class);
        if(constructor!=null){
            final PageParameters nullSafeParams=(parameters==null)?new PageParameters():parameters;
            return this.processPage(this.newPage(constructor,nullSafeParams),nullSafeParams);
        }
        return this.processPage(this.newPage(pageClass),parameters);
    }
    private <C extends IRequestablePage> Constructor<?> constructor(final Class<C> pageClass,final Class<PageParameters> argumentType){
        Constructor<?> constructor=(Constructor<?>)this.constructorForClass.get(pageClass);
        if(constructor==null){
            try{
                constructor=(Constructor<?>)pageClass.getConstructor(new Class[] { argumentType });
                final Constructor<?> tmpConstructor=this.constructorForClass.putIfAbsent(pageClass,constructor);
                if(tmpConstructor!=null){
                    constructor=tmpConstructor;
                }
                DefaultPageFactory.log.debug("Found constructor for Page of type '{}' and argument of type '{}'.",pageClass,argumentType);
            }
            catch(NoSuchMethodException e){
                DefaultPageFactory.log.debug("Page of type '{}' has not visible constructor with an argument of type '{}'.",pageClass,argumentType);
                return null;
            }
        }
        return constructor;
    }
    private Page newPage(final Constructor<?> constructor,final Object argument){
        try{
            if(argument!=null){
                return (Page)constructor.newInstance(new Object[] { argument });
            }
            return (Page)constructor.newInstance(new Object[0]);
        }
        catch(InstantiationException e){
            throw new WicketRuntimeException(this.createDescription(constructor,argument),e);
        }
        catch(IllegalAccessException e2){
            throw new WicketRuntimeException(this.createDescription(constructor,argument),e2);
        }
        catch(InvocationTargetException e3){
            if(e3.getTargetException() instanceof RequestHandlerStack.ReplaceHandlerException||e3.getTargetException() instanceof AuthorizationException||e3.getTargetException() instanceof MarkupException){
                throw (RuntimeException)e3.getTargetException();
            }
            throw new WicketRuntimeException(this.createDescription(constructor,argument),e3);
        }
    }
    private Page processPage(final Page page,final PageParameters pageParameters){
        if(pageParameters!=null&&page.getPageParameters()!=pageParameters){
            page.getPageParameters().overwriteWith(pageParameters);
        }
        page.setWasCreatedBookmarkable(true);
        return page;
    }
    private String createDescription(final Constructor<?> constructor,final Object argument){
        String msg;
        if(argument!=null){
            msg="Can't instantiate page using constructor '"+constructor+"' and argument '"+argument;
        }
        else{
            msg="Can't instantiate page using constructor '"+constructor;
        }
        return msg+"'. Might be it doesn't exist, may be it is not visible (public).";
    }
    public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass){
        Boolean bookmarkable=(Boolean)this.pageToBookmarkableCache.get(pageClass.getName());
        if(bookmarkable==null){
            try{
                if(pageClass.getConstructor(new Class[0])!=null){
                    bookmarkable=Boolean.TRUE;
                }
            }
            catch(Exception ignore){
                try{
                    if(pageClass.getConstructor(new Class[] { PageParameters.class })!=null){
                        bookmarkable=Boolean.TRUE;
                    }
                }
                catch(Exception ex){
                }
            }
            if(bookmarkable==null){
                bookmarkable=Boolean.FALSE;
            }
            final Boolean tmpBookmarkable=this.pageToBookmarkableCache.putIfAbsent(pageClass.getName(),bookmarkable);
            if(tmpBookmarkable!=null){
                bookmarkable=tmpBookmarkable;
            }
        }
        return bookmarkable;
    }
    static{
        log=LoggerFactory.getLogger(DefaultPageFactory.class);
    }
}
