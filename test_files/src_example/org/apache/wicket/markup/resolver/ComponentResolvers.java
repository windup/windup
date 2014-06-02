package org.apache.wicket.markup.resolver;

import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import java.util.*;

public class ComponentResolvers{
    public static Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag,final ResolverFilter filter){
        Component component=resolveByComponentHierarchy(container,markupStream,tag);
        if(component==null){
            component=resolveByApplication(container,markupStream,tag,filter);
        }
        return component;
    }
    private static Component resolveByApplication(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag,final ResolverFilter filter){
        for(final IComponentResolver resolver : Application.get().getPageSettings().getComponentResolvers()){
            if(filter==null||!filter.ignoreResolver(resolver)){
                final Component component=resolver.resolve(container,markupStream,tag);
                if(component!=null){
                    return component;
                }
                continue;
            }
        }
        return null;
    }
    private static Component resolveByComponentHierarchy(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        for(Component cursor=container;cursor!=null;cursor=cursor.getParent()){
            if(cursor instanceof IComponentResolver){
                final IComponentResolver resolver=(IComponentResolver)cursor;
                final Component component=resolver.resolve(container,markupStream,tag);
                if(component!=null){
                    return component;
                }
            }
        }
        return null;
    }
    public interface ResolverFilter{
        boolean ignoreResolver(IComponentResolver p0);
    }
}
