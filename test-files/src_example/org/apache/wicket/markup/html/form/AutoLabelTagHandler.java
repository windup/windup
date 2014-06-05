package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.string.*;
import java.text.*;

public class AutoLabelTagHandler extends AbstractMarkupFilter{
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag==null||tag.isClose()||tag instanceof WicketTag){
            return tag;
        }
        String related=tag.getAttribute("wicket:for");
        if(related==null){
            return tag;
        }
        related=related.trim();
        if(Strings.isEmpty((CharSequence)related)){
            throw new ParseException("Tag contains an empty wicket:for attribute",tag.getPos());
        }
        if(!"label".equalsIgnoreCase(tag.getName())){
            throw new ParseException("Attribute wicket:for can only be attached to <label> tag",tag.getPos());
        }
        if(tag.getId()!=null){
            throw new ParseException("Attribute wicket:for cannot be used in conjunction with wicket:id",tag.getPos());
        }
        tag.setId(this.getClass().getName());
        tag.setModified(true);
        tag.setAutoComponentTag(true);
        return tag;
    }
}
