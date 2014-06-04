package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.parser.filter.*;

public abstract class Panel extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    public static final String PANEL="panel";
    public Panel(final String id){
        super(id);
    }
    public Panel(final String id,final IModel<?> model){
        super(id,model);
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return new PanelMarkupSourcingStrategy(false);
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("panel");
    }
}
