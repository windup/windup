package org.apache.wicket.markup.resolver;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.parser.filter.*;

public class FragmentResolver implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String FRAGMENT="fragment";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag){
            final WicketTag wTag=(WicketTag)tag;
            if(wTag.isFragementTag()){
                return new WebComponent(wTag.getId()).setVisible(false);
            }
        }
        return null;
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("fragment");
    }
}
