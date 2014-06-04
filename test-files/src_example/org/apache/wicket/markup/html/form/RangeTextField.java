package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;

public class RangeTextField<N extends Number> extends NumberTextField<N>{
    private static final long serialVersionUID=1L;
    public RangeTextField(final String id){
        this(id,(IModel)null);
    }
    public RangeTextField(final String id,final IModel<N> model){
        this(id,model,null);
    }
    public RangeTextField(final String id,final IModel<N> model,final Class<N> type){
        super(id,model,type);
    }
    protected String getInputType(){
        return "range";
    }
}
