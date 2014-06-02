package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class Fragment extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private final String associatedMarkupId;
    private final MarkupContainer markupProvider;
    public Fragment(final String id,final String markupId,final MarkupContainer markupProvider){
        this(id,markupId,markupProvider,null);
    }
    public Fragment(final String id,final String markupId,final MarkupContainer markupProvider,final IModel<?> model){
        super(id,model);
        this.associatedMarkupId=(String)Args.notNull((Object)markupId,"markupId");
        this.markupProvider=markupProvider;
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return new FragmentMarkupSourcingStrategy(this.associatedMarkupId,this.markupProvider){
            public IMarkupFragment chooseMarkup(final Component component){
                return Fragment.this.chooseMarkup(this.getMarkupProvider(component));
            }
        };
    }
    protected IMarkupFragment chooseMarkup(final MarkupContainer provider){
        return provider.getMarkup(null);
    }
    public final String getAssociatedMarkupId(){
        return this.associatedMarkupId;
    }
}
