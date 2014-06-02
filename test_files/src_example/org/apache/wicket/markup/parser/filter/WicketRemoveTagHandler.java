package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;
import java.text.*;

public final class WicketRemoveTagHandler extends AbstractMarkupFilter{
    public static final String REMOVE="remove";
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(!(tag instanceof WicketTag)||!((WicketTag)tag).isRemoveTag()){
            return tag;
        }
        if(tag.isOpenClose()){
            throw new WicketParseException("Wicket remove tag must not be an open-close tag:",tag);
        }
        MarkupElement markupElement;
        while((markupElement=this.getNextFilter().nextElement())!=null){
            if(!(markupElement instanceof ComponentTag)){
                continue;
            }
            final ComponentTag closeTag=(ComponentTag)markupElement;
            if(closeTag.getId()==null){
                continue;
            }
            if(closeTag.closes(tag)){
                tag.setIgnore(true);
                return tag;
            }
            throw new WicketParseException("Markup remove regions must not contain Wicket component tags:",closeTag);
        }
        throw new WicketParseException("Did not find close tag for markup remove region. Open tag:",tag);
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("remove");
    }
}
