package org.apache.wicket.validation;

import org.apache.wicket.behavior.*;
import org.apache.wicket.util.lang.*;

public class ValidatorAdapter<T> extends Behavior implements IValidator<T>{
    private static final long serialVersionUID=1L;
    private final IValidator<T> validator;
    public ValidatorAdapter(final IValidator<T> validator){
        super();
        Args.notNull((Object)validator,"validator");
        this.validator=validator;
    }
    public void validate(final IValidatable<T> validatable){
        this.validator.validate(validatable);
    }
    public IValidator<T> getValidator(){
        return this.validator;
    }
}
