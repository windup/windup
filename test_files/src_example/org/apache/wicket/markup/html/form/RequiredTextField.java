package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;

public class RequiredTextField<T> extends TextField<T>{
    private static final long serialVersionUID=1L;
    public RequiredTextField(final String id){
        super(id);
        this.setRequired(true);
    }
    public RequiredTextField(final String id,final Class<T> type){
        super(id,type);
        this.setRequired(true);
    }
    public RequiredTextField(final String id,final IModel<T> model){
        super(id,model);
        this.setRequired(true);
    }
    public RequiredTextField(final String id,final IModel<T> model,final Class<T> type){
        super(id,model,type);
        this.setRequired(true);
    }
}
