package org.apache.wicket.markup.html.panel;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class BorderMarkupSourcingStrategy extends AssociatedMarkupSourcingStrategy{
    public BorderMarkupSourcingStrategy(){
        super("border");
    }
    public void onComponentTagBody(final Component component,final MarkupStream markupStream,final ComponentTag openTag){
        this.renderAssociatedMarkup(component);
        markupStream.skipToMatchingCloseTag(openTag);
    }
    public IMarkupFragment getMarkup(final MarkupContainer container,final Component child){
        return null;
    }
}
