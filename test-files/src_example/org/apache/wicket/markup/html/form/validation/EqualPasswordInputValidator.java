package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.markup.html.form.*;

public class EqualPasswordInputValidator extends EqualInputValidator{
    private static final long serialVersionUID=1L;
    public EqualPasswordInputValidator(final FormComponent<?> formComponent1,final FormComponent<?> formComponent2){
        super(formComponent1,formComponent2);
    }
}
