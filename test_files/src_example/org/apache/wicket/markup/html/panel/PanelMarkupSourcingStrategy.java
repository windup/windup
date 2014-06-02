package org.apache.wicket.markup.html.panel;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class PanelMarkupSourcingStrategy extends AssociatedMarkupSourcingStrategy{
    private final boolean allowWicketComponentsInBodyMarkup;
    public PanelMarkupSourcingStrategy(final String wicketTagName,final boolean allowWicketComponentsInBodyMarkup){
        super(wicketTagName);
        this.allowWicketComponentsInBodyMarkup=allowWicketComponentsInBodyMarkup;
    }
    public PanelMarkupSourcingStrategy(final boolean allowWicketComponentsInBodyMarkup){
        this("panel",allowWicketComponentsInBodyMarkup);
    }
    public void onComponentTagBody(final Component component,final MarkupStream markupStream,final ComponentTag openTag){
        if(this.allowWicketComponentsInBodyMarkup){
            markupStream.skipToMatchingCloseTag(openTag);
        }
        else if(markupStream.getPreviousTag().isOpen()){
            markupStream.skipRawMarkup();
            if(!markupStream.get().closes(openTag)){
                final StringBuilder msg=new StringBuilder();
                msg.append("Close tag not found for tag: ").append(openTag.toString()).append(". For ").append(component.getClass().getSimpleName()).append(" Components only raw markup is allow in between the tags but not ").append("other Wicket Component. Component: ").append(component.toString());
                throw new MarkupException(markupStream,msg.toString());
            }
        }
        this.renderAssociatedMarkup(component);
    }
}
