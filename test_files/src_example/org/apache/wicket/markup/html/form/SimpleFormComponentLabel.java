package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class SimpleFormComponentLabel extends FormComponentLabel{
    private static final long serialVersionUID=1L;
    public SimpleFormComponentLabel(final String id,final LabeledWebMarkupContainer labelProvider){
        super(id,labelProvider);
        if(labelProvider.getLabel()==null){
            throw new IllegalStateException("Provided form component does not have a label set. Use FormComponent.setLabel(IModel) to set the model that will feed this label");
        }
        this.setDefaultModel(labelProvider.getLabel());
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)this.getDefaultModelObjectAsString());
    }
}
