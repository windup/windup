package org.apache.wicket.markup.html.border;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.*;

public abstract class BorderPanel extends Panel{
    private static final long serialVersionUID=1L;
    private Body body;
    public BorderPanel(final String id){
        this(id,null);
    }
    public BorderPanel(final String id,final IModel<?> model){
        super(id,model);
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return new PanelMarkupSourcingStrategy(true);
    }
    public final Body setBodyContainer(final Body body){
        return this.body=body;
    }
    public final Body getBodyContainer(){
        return this.body;
    }
    public final Body newBodyContainer(final String id){
        return this.body=new Body(id,this);
    }
}
