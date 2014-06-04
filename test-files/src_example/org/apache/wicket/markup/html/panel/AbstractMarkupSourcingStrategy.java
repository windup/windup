package org.apache.wicket.markup.html.panel;

import org.apache.wicket.*;
import org.apache.wicket.markup.resolver.*;
import java.util.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.internal.*;

public abstract class AbstractMarkupSourcingStrategy implements IMarkupSourcingStrategy{
    public abstract IMarkupFragment getMarkup(final MarkupContainer p0,final Component p1);
    protected IMarkupFragment searchMarkupInTransparentResolvers(final MarkupContainer container,final Component child){
        IMarkupFragment markup=null;
        for(final Component ch : container){
            if(ch!=child&&ch instanceof MarkupContainer&&ch instanceof IComponentResolver){
                markup=((MarkupContainer)ch).getMarkup(child);
                if(markup!=null){
                    break;
                }
                continue;
            }
        }
        return markup;
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
        if(tag.isOpenClose()){
            tag.setType(XmlTag.TagType.OPEN);
        }
    }
    public void onComponentTagBody(final Component component,final MarkupStream markupStream,final ComponentTag openTag){
        if(markupStream.getPreviousTag().isOpen()){
            markupStream.skipRawMarkup();
            if(!markupStream.get().closes(openTag)){
                throw new MarkupException(markupStream,"Close tag not found for tag: "+openTag.toString()+". For "+component.getClass().getSimpleName()+" Components only raw markup is allow in between the tags but not other Wicket Component."+". Component: "+component.toString());
            }
        }
    }
    public void renderHead(final Component component,final HtmlHeaderContainer container){
    }
}
