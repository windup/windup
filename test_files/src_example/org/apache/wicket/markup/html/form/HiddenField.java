package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;

public class HiddenField<T> extends TextField<T>{
    private static final long serialVersionUID=1L;
    public HiddenField(final String id){
        super(id);
    }
    public HiddenField(final String id,final Class<T> type){
        super(id,type);
    }
    public HiddenField(final String id,final IModel<T> model){
        super(id,model);
    }
    public HiddenField(final String id,final IModel<T> model,final Class<T> type){
        super(id,model,type);
    }
    protected String getInputType(){
        return "hidden";
    }
}
