package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.filter.*;

public abstract class FormComponentPanel<T> extends FormComponent<T>{
    private static final long serialVersionUID=1L;
    public FormComponentPanel(final String id){
        super(id);
    }
    public FormComponentPanel(final String id,final IModel<T> model){
        super(id,model);
    }
    public boolean checkRequired(){
        return true;
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return new PanelMarkupSourcingStrategy(false);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.remove("name");
        tag.remove("disabled");
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("panel");
    }
}
