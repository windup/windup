package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public interface IMarkupSourcingStrategy{
    void renderHead(Component p0,HtmlHeaderContainer p1);
    void onComponentTag(Component p0,ComponentTag p1);
    void onComponentTagBody(Component p0,MarkupStream p1,ComponentTag p2);
    IMarkupFragment getMarkup(MarkupContainer p0,Component p1);
}
