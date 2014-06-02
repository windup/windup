package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.resolver.*;
import java.util.*;
import java.text.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.internal.*;

public final class InlineEnclosureHandler extends AbstractMarkupFilter implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String INLINE_ENCLOSURE_ID_PREFIX="InlineEnclosure-";
    public static final String INLINE_ENCLOSURE_ATTRIBUTE_NAME="wicket:enclosure";
    private Stack<ComponentTag> enclosures;
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag instanceof WicketTag){
            return tag;
        }
        final String enclosureAttr=getInlineEnclosureAttribute(tag);
        if(enclosureAttr!=null){
            if(!tag.isOpen()){
                throw new ParseException("Open-close tags don't make sense for InlineEnclosure. Tag:"+tag.toString(),tag.getPos());
            }
            final String htmlId=tag.getAttribute("id");
            if(tag.getId()!=null&&!Strings.isEmpty((CharSequence)htmlId)&&!htmlId.equals(tag.getId())){
                throw new ParseException("Make sure that 'id' and 'wicket:id' are the same if both are provided. Tag:"+tag.toString(),tag.getPos());
            }
            if(Strings.isEmpty((CharSequence)tag.getId())){
                if(Strings.isEmpty((CharSequence)htmlId)){
                    tag.setId("InlineEnclosure-");
                }
                else{
                    tag.setId(htmlId);
                }
                tag.setAutoComponentTag(true);
                tag.setModified(true);
            }
            if(this.enclosures==null){
                this.enclosures=(Stack<ComponentTag>)new Stack();
            }
            this.enclosures.push(tag);
        }
        else if(this.enclosures!=null&&this.enclosures.size()>0){
            if(tag.isOpen()&&tag.getId()!=null&&!(tag instanceof WicketTag)&&!tag.isAutoComponentTag()){
                for(int i=this.enclosures.size()-1;i>=0;--i){
                    final ComponentTag lastEnclosure=(ComponentTag)this.enclosures.get(i);
                    final String attr=getInlineEnclosureAttribute(lastEnclosure);
                    if(Strings.isEmpty((CharSequence)attr)){
                        lastEnclosure.getAttributes().put((Object)"wicket:enclosure",(Object)tag.getId());
                        lastEnclosure.setModified(true);
                    }
                }
            }
            else if(tag.isClose()&&tag.closes((MarkupElement)this.enclosures.peek())){
                final ComponentTag lastEnclosure2=(ComponentTag)this.enclosures.pop();
                final String attr2=getInlineEnclosureAttribute(lastEnclosure2);
                if(Strings.isEmpty((CharSequence)attr2)){
                    throw new ParseException("Did not find any child for InlineEnclosure. Tag:"+lastEnclosure2.toString(),tag.getPos());
                }
            }
        }
        return tag;
    }
    public static final String getInlineEnclosureAttribute(final ComponentTag tag){
        return tag.getAttributes().getString("wicket:enclosure");
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        final String inlineEnclosureChildId=getInlineEnclosureAttribute(tag);
        if(!Strings.isEmpty((CharSequence)inlineEnclosureChildId)){
            final String id=tag.getId()+container.getPage().getAutoIndex();
            return new InlineEnclosure(id,inlineEnclosureChildId);
        }
        return null;
    }
}
