package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.resolver.*;
import java.util.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.internal.*;

public final class EnclosureHandler extends AbstractMarkupFilter implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String ENCLOSURE="enclosure";
    public static final String CHILD_ATTRIBUTE="child";
    private Stack<ComponentTag> stack;
    private String childId;
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        final boolean isWicketTag=tag instanceof WicketTag;
        final boolean isEnclosureTag=isWicketTag&&((WicketTag)tag).isEnclosureTag();
        if(isEnclosureTag){
            if(tag.isOpen()){
                if(this.stack==null){
                    this.stack=(Stack<ComponentTag>)new Stack();
                }
                this.stack.push(tag);
            }
            else{
                if(!tag.isClose()){
                    throw new WicketParseException("Open-close tag not allowed for Enclosure:",tag);
                }
                if(this.stack==null){
                    throw new WicketParseException("Missing open tag for Enclosure:",tag);
                }
                final ComponentTag lastEnclosure=(ComponentTag)this.stack.pop();
                if(this.childId!=null){
                    lastEnclosure.put("child",(CharSequence)this.childId);
                    lastEnclosure.setModified(true);
                    this.childId=null;
                }
                if(this.stack.size()==0){
                    this.stack=null;
                }
            }
        }
        else if(tag.getId()!=null&&!isWicketTag&&this.stack!=null&&!tag.isAutoComponentTag()){
            final ComponentTag lastEnclosure=(ComponentTag)this.stack.lastElement();
            if(lastEnclosure.getAttribute("child")==null){
                if(this.childId!=null){
                    throw new WicketParseException("Use <wicket:enclosure child='xxx'> to name the child component:",tag);
                }
                this.childId=tag.getId();
            }
        }
        return tag;
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag&&((WicketTag)tag).isEnclosureTag()){
            return new Enclosure(tag.getId()+container.getPage().getAutoIndex(),(CharSequence)tag.getAttribute("child"));
        }
        return null;
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("enclosure");
    }
}
