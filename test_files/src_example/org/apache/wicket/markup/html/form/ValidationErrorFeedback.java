package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;
import org.apache.wicket.validation.*;

public class ValidationErrorFeedback implements IClusterable{
    private static final long serialVersionUID=1L;
    private final IValidationError error;
    private final String message;
    public ValidationErrorFeedback(final IValidationError error,final String message){
        super();
        if(error==null){
            throw new IllegalArgumentException("Argument [[error]] cannot be null");
        }
        this.error=error;
        this.message=message;
    }
    public static long getSerialVersionUID(){
        return 1L;
    }
    public IValidationError getError(){
        return this.error;
    }
    public String getMessage(){
        return this.message;
    }
    public String toString(){
        return this.message;
    }
}
