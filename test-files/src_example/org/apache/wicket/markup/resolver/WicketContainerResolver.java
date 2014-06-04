package org.apache.wicket.markup.resolver;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;
import org.apache.wicket.markup.parser.filter.*;

public class WicketContainerResolver implements IComponentResolver{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public static final String CONTAINER="container";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag){
            return container.get(tag.getId());
        }
        return null;
    }
    static{
        log=LoggerFactory.getLogger(WicketContainerResolver.class);
        WicketTagIdentifier.registerWellKnownTagName("container");
    }
}
