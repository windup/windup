package org.apache.wicket.markup.resolver;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.parser.filter.*;

public class MarkupInheritanceResolver implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String CHILD="child";
    public static final String EXTEND="extend";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag){
            final WicketTag wicketTag=(WicketTag)tag;
            if(wicketTag.isExtendTag()||wicketTag.isChildTag()){
                final String id=wicketTag.getId()+container.getPage().getAutoIndex();
                return new TransparentWebMarkupContainer(id);
            }
        }
        return null;
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("extend");
        WicketTagIdentifier.registerWellKnownTagName("child");
    }
}
