package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.html.*;
import java.util.*;

public abstract class AbstractHeaderRenderStrategy implements IHeaderRenderStrategy{
    public static IHeaderRenderStrategy get(){
        final String className=System.getProperty("Wicket_HeaderRenderStrategy");
        if(className!=null){
            Class<?> clazz=null;
            try{
                clazz=Application.get().getApplicationSettings().getClassResolver().resolveClass(className);
                if(clazz!=null){
                    return (IHeaderRenderStrategy)clazz.newInstance();
                }
            }
            catch(ClassNotFoundException ex){
            }
            catch(InstantiationException ex2){
            }
            catch(IllegalAccessException ex3){
            }
        }
        return new ChildFirstHeaderRenderStrategy();
    }
    public void renderHeader(final HtmlHeaderContainer headerContainer,final Component rootComponent){
        Args.notNull((Object)headerContainer,"headerContainer");
        Args.notNull((Object)rootComponent,"rootComponent");
        this.renderApplicationLevelHeaders(headerContainer);
        this.renderRootComponent(headerContainer,rootComponent);
        this.renderChildHeaders(headerContainer,rootComponent);
    }
    protected void renderRootComponent(final HtmlHeaderContainer headerContainer,final Component rootComponent){
        rootComponent.renderHead(headerContainer);
    }
    protected abstract void renderChildHeaders(final HtmlHeaderContainer p0,final Component p1);
    protected final void renderApplicationLevelHeaders(final HtmlHeaderContainer headerContainer){
        Args.notNull((Object)headerContainer,"headerContainer");
        if(Application.exists()){
            for(final IHeaderContributor listener : Application.get().getHeaderContributorListenerCollection()){
                listener.renderHead(headerContainer.getHeaderResponse());
            }
        }
    }
}
