package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.*;

public class Body extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private final MarkupContainer markupProvider;
    public Body(final String id,final IModel<?> model,final MarkupContainer markupProvider){
        super(id,model);
        Args.notNull((Object)markupProvider,"markupProvider");
        this.markupProvider=markupProvider;
    }
    public Body(final String id,final MarkupContainer markupProvider){
        this(id,null,markupProvider);
    }
    public IMarkupFragment getMarkup(){
        return this.markupProvider.getMarkup();
    }
}
