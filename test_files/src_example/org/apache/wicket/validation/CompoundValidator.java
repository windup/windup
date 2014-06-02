package org.apache.wicket.validation;

import org.apache.wicket.behavior.*;
import org.apache.wicket.util.lang.*;
import java.util.*;

public class CompoundValidator<T> extends Behavior implements IValidator<T>{
    private static final long serialVersionUID=1L;
    private final List<IValidator<T>> validators;
    public CompoundValidator(){
        super();
        this.validators=(List<IValidator<T>>)new ArrayList(2);
    }
    public final CompoundValidator<T> add(final IValidator<T> validator){
        Args.notNull((Object)validator,"validator");
        this.validators.add(validator);
        return this;
    }
    public final void validate(final IValidatable<T> validatable){
        final Iterator<IValidator<T>> it=(Iterator<IValidator<T>>)this.validators.iterator();
        while(it.hasNext()&&validatable.isValid()){
            ((IValidator)it.next()).validate(validatable);
        }
    }
    public final List<IValidator<T>> getValidators(){
        return (List<IValidator<T>>)Collections.unmodifiableList(this.validators);
    }
}
