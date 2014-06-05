package org.apache.wicket.validation.validator;

import org.apache.wicket.behavior.*;
import org.apache.wicket.validation.*;
import org.apache.wicket.util.lang.*;

public class MinimumValidator<Z extends Comparable> extends Behavior implements IValidator<Z>{
    private static final long serialVersionUID=1L;
    private final Z minimum;
    public MinimumValidator(final Z minimum){
        super();
        this.minimum=(Comparable)minimum;
    }
    public void validate(final IValidatable<Z> validatable){
        final Z value=validatable.getValue();
        if(((Comparable)value).compareTo(this.minimum)<0){
            final ValidationError error=new ValidationError();
            error.addMessageKey(this.resourceKey());
            error.setVariable("minimum",this.minimum);
            validatable.error(error);
        }
    }
    public Z getMinimum(){
        return (Z)this.minimum;
    }
    protected String resourceKey(){
        return Classes.simpleName(MinimumValidator.class);
    }
}
