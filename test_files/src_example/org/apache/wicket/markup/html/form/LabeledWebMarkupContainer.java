package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;

public abstract class LabeledWebMarkupContainer extends WebMarkupContainer implements ILabelProvider<String>{
    private static final long serialVersionUID=1L;
    private IModel<String> labelModel;
    protected void onDetach(){
        super.onDetach();
        if(this.labelModel!=null){
            this.labelModel.detach();
            if(this.labelModel instanceof IWrapModel){
                ((IWrapModel)this.labelModel).getWrappedModel().detach();
            }
        }
    }
    public LabeledWebMarkupContainer(final String id){
        super(id);
        this.labelModel=null;
    }
    public LabeledWebMarkupContainer(final String id,final IModel<?> model){
        super(id,model);
        this.labelModel=null;
    }
    public IModel<String> getLabel(){
        return this.labelModel;
    }
    protected void setLabelInternal(final IModel<String> labelModel){
        this.labelModel=this.wrap(labelModel);
    }
}
