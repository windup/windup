package org.apache.wicket.markup.html.panel;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class FragmentMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy{
    private String markupId;
    private final MarkupContainer markupProvider;
    public FragmentMarkupSourcingStrategy(final String markupId,final MarkupContainer markupProvider){
        super();
        Args.notNull((Object)markupId,"markupId");
        this.markupId=markupId;
        this.markupProvider=markupProvider;
    }
    public void onComponentTagBody(final Component component,final MarkupStream markupStream,final ComponentTag openTag){
        super.onComponentTagBody(component,markupStream,openTag);
        final MarkupStream stream=new MarkupStream(this.getMarkup((MarkupContainer)component,null));
        final ComponentTag fragmentOpenTag=stream.getTag();
        if(!fragmentOpenTag.isOpenClose()){
            stream.next();
            component.onComponentTagBody(stream,fragmentOpenTag);
        }
    }
    protected final MarkupContainer getMarkupProvider(final Component component){
        return (this.markupProvider!=null)?this.markupProvider:component.getParent();
    }
    public IMarkupFragment chooseMarkup(final Component component){
        return this.getMarkupProvider(component).getMarkup(null);
    }
    public IMarkupFragment getMarkup(final MarkupContainer container,final Component child){
        IMarkupFragment markup=this.chooseMarkup(container);
        if(markup==null){
            throw new MarkupException("The fragments markup provider has no associated markup. No markup to search for fragment markup with id: "+this.markupId);
        }
        IMarkupFragment childMarkup=markup.find(this.markupId);
        if(childMarkup==null){
            final MarkupContainer markupProvider=this.getMarkupProvider(container);
            final Markup associatedMarkup=markupProvider.getAssociatedMarkup();
            if(associatedMarkup!=null){
                markup=associatedMarkup;
                if(markup!=null){
                    childMarkup=markup.find(this.markupId);
                }
            }
        }
        if(childMarkup==null){
            throw new MarkupNotFoundException("No Markup found for Fragment "+this.markupId+" in providing markup container "+this.getMarkupProvider(container));
        }
        if(child==null){
            return childMarkup;
        }
        return childMarkup.find(child.getId());
    }
}
