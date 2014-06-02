package org.apache.wicket.validation.validator;

import org.apache.wicket.behavior.*;
import org.apache.wicket.validation.*;
import org.apache.wicket.util.lang.*;

public class MaximumValidator<Z extends Comparable> extends Behavior implements IValidator<Z>{
    private static final long serialVersionUID=1L;
    private final Z maximum;
    public MaximumValidator(final Z maximum){
        super();
        this.maximum=(Comparable)maximum;
    }
    public void validate(final IValidatable<Z> validatable){
        final Z value=validatable.getValue();
        if(((Comparable)value).compareTo(this.maximum)>0){
            final ValidationError error=new ValidationError();
            error.addMessageKey(this.resourceKey());
            error.setVariable("maximum",this.maximum);
            validatable.error(error);
        }
    }
    public Z getMaximum(){
        return (Z)this.maximum;
    }
    protected String resourceKey(){
        return Classes.simpleName(MaximumValidator.class);
    }
}
