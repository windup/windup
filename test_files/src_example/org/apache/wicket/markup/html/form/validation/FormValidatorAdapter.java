package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.form.*;

public class FormValidatorAdapter extends Behavior implements IFormValidator{
    private static final long serialVersionUID=1L;
    private final IFormValidator validator;
    public FormValidatorAdapter(final IFormValidator validator){
        super();
        this.validator=validator;
    }
    public FormComponent<?>[] getDependentFormComponents(){
        return this.validator.getDependentFormComponents();
    }
    public void validate(final Form<?> form){
        this.validator.validate(form);
    }
    public IFormValidator getValidator(){
        return this.validator;
    }
}
