package org.apache.wicket.validation.validator;

import org.apache.wicket.behavior.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.validation.*;
import java.util.*;

public abstract class AbstractValidator<T> extends Behavior implements INullAcceptingValidator<T>,IClusterable{
    private static final long serialVersionUID=1L;
    public boolean validateOnNullValue(){
        return false;
    }
    protected abstract void onValidate(final IValidatable<T> p0);
    public final void validate(final IValidatable<T> validatable){
        if(validatable.getValue()!=null||this.validateOnNullValue()){
            this.onValidate(validatable);
        }
    }
    public void error(final IValidatable<T> validatable){
        this.error(validatable,this.resourceKey(),this.variablesMap(validatable));
    }
    public void error(final IValidatable<T> validatable,final String resourceKey){
        if(resourceKey==null){
            throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
        }
        this.error(validatable,resourceKey,this.variablesMap(validatable));
    }
    public void error(final IValidatable<T> validatable,final Map<String,Object> vars){
        if(vars==null){
            throw new IllegalArgumentException("Argument [[vars]] cannot be null");
        }
        this.error(validatable,this.resourceKey(),vars);
    }
    public void error(final IValidatable<T> validatable,final String resourceKey,final Map<String,Object> vars){
        if(validatable==null){
            throw new IllegalArgumentException("Argument [[validatable]] cannot be null");
        }
        if(vars==null){
            throw new IllegalArgumentException("Argument [[vars]] cannot be null");
        }
        if(resourceKey==null){
            throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
        }
        final ValidationError error=new ValidationError().addMessageKey(resourceKey);
        final String defaultKey=Classes.simpleName(this.getClass());
        if(!resourceKey.equals(defaultKey)){
            error.addMessageKey(defaultKey);
        }
        error.setVariables(vars);
        validatable.error(error);
    }
    protected String resourceKey(){
        return Classes.simpleName(this.getClass());
    }
    protected Map<String,Object> variablesMap(final IValidatable<T> validatable){
        return (Map<String,Object>)new HashMap(1);
    }
}
