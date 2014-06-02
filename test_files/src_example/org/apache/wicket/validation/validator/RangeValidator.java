package org.apache.wicket.validation.validator;

import org.apache.wicket.behavior.*;
import org.apache.wicket.validation.*;
import org.apache.wicket.util.lang.*;

public class RangeValidator<Z extends Comparable> extends Behavior implements IValidator<Z>{
    private static final long serialVersionUID=1L;
    private Z minimum;
    private Z maximum;
    public RangeValidator(final Z minimum,final Z maximum){
        super();
        this.setRange(minimum,maximum);
    }
    protected RangeValidator(){
        super();
    }
    protected final void setRange(final Z minimum,final Z maximum){
        this.minimum=(Comparable)minimum;
        this.maximum=(Comparable)maximum;
    }
    public void validate(final IValidatable<Z> validatable){
        final Z value=validatable.getValue();
        final Z min=this.getMinimum();
        final Z max=this.getMaximum();
        if((min!=null&&((Comparable)value).compareTo(min)<0)||(max!=null&&((Comparable)value).compareTo(max)>0)){
            final ValidationError error=new ValidationError();
            error.addMessageKey(this.resourceKey());
            if(min!=null){
                error.setVariable("minimum",min);
            }
            if(max!=null){
                error.setVariable("maximum",max);
            }
            validatable.error(error);
        }
    }
    public Z getMinimum(){
        return (Z)this.minimum;
    }
    public Z getMaximum(){
        return (Z)this.maximum;
    }
    protected String resourceKey(){
        return Classes.simpleName(RangeValidator.class);
    }
}
