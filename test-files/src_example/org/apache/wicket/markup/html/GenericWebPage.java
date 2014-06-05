package org.apache.wicket.markup.html;

import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.*;

public class GenericWebPage<T> extends WebPage{
    private static final long serialVersionUID=1L;
    protected GenericWebPage(){
        super();
    }
    protected GenericWebPage(final IModel<T> model){
        super(model);
    }
    protected GenericWebPage(final PageParameters parameters){
        super(parameters);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T modelObject){
        this.setDefaultModelObject(modelObject);
    }
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
}
