package org.apache.wicket.validation;

import org.apache.wicket.model.*;
import java.util.*;

public class Validatable<T> implements IValidatable<T>{
    private T value;
    private ArrayList<IValidationError> errors;
    private IModel<T> model;
    public Validatable(){
        super();
    }
    public Validatable(final T value){
        super();
        this.value=value;
    }
    public void setModel(final IModel<T> model){
        this.model=model;
    }
    public void setValue(final T value){
        this.value=value;
    }
    public T getValue(){
        return this.value;
    }
    public void error(final IValidationError error){
        if(this.errors==null){
            this.errors=(ArrayList<IValidationError>)new ArrayList();
        }
        this.errors.add(error);
    }
    public List<IValidationError> getErrors(){
        if(this.errors==null){
            return (List<IValidationError>)Collections.emptyList();
        }
        return (List<IValidationError>)Collections.unmodifiableList(this.errors);
    }
    public boolean isValid(){
        return this.errors==null;
    }
    public IModel<T> getModel(){
        return this.model;
    }
}
