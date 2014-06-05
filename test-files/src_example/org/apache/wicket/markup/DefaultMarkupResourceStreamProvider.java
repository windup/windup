package org.apache.wicket.markup;

import org.apache.wicket.util.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.locator.*;
import java.util.*;
import org.slf4j.*;

public class DefaultMarkupResourceStreamProvider implements IMarkupResourceStreamProvider{
    private static final Logger log;
    public IResourceStream getMarkupResourceStream(final MarkupContainer container,Class<?> containerClass){
        final IResourceStreamLocator locator=Application.get().getResourceSettings().getResourceStreamLocator();
        final String style=container.getStyle();
        final String variation=container.getVariation();
        final Locale locale=container.getLocale();
        final MarkupType markupType=container.getMarkupType();
        final String ext=(markupType!=null)?markupType.getExtension():null;
        while(containerClass!=MarkupContainer.class){
            final String path=containerClass.getName().replace('.','/');
            final IResourceStream resourceStream=locator.locate((Class<?>)container.getClass(),path,style,variation,locale,ext,false);
            if(resourceStream!=null){
                return (IResourceStream)new MarkupResourceStream(resourceStream,new ContainerInfo(container),containerClass);
            }
            containerClass=(Class<?>)containerClass.getSuperclass();
        }
        return null;
    }
    static{
        log=LoggerFactory.getLogger(DefaultMarkupResourceStreamProvider.class);
    }
}
