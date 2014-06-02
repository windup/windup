package org.apache.wicket.markup.html;

import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public final class HeaderPartContainer extends WebMarkupContainer implements IComponentResolver{
    private static final long serialVersionUID=1L;
    private final MarkupContainer container;
    private final String scope;
    public HeaderPartContainer(final String id,final MarkupContainer container,final IMarkupFragment markup){
        super(id);
        Args.notNull((Object)container,"container");
        Args.notNull((Object)markup,"markup");
        this.setMarkup(markup);
        this.container=container;
        this.scope=this.getScopeFromMarkup();
        this.setRenderBodyOnly(true);
    }
    private String getScopeFromMarkup(){
        final IMarkupFragment markup=this.getMarkup();
        final String namespace=markup.getMarkupResourceStream().getWicketNamespace();
        final ComponentTag tag=(ComponentTag)markup.get(0);
        return tag.getAttributes().getString(namespace+":scope");
    }
    public final String getScope(){
        return this.scope;
    }
    public final Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        return this.container.get(tag.getId());
    }
}
