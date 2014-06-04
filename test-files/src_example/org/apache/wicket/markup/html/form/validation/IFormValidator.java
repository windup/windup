package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.*;
import org.apache.wicket.markup.html.form.*;

public interface IFormValidator extends IClusterable{
    FormComponent<?>[] getDependentFormComponents();
    void validate(Form<?> p0);
}
