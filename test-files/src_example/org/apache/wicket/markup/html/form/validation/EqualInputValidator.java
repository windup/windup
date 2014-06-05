package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.util.lang.*;

public class EqualInputValidator extends AbstractFormValidator{
    private static final long serialVersionUID=1L;
    private final FormComponent<?>[] components;
    public EqualInputValidator(final FormComponent<?> formComponent1,final FormComponent<?> formComponent2){
        super();
        if(formComponent1==null){
            throw new IllegalArgumentException("argument formComponent1 cannot be null");
        }
        if(formComponent2==null){
            throw new IllegalArgumentException("argument formComponent2 cannot be null");
        }
        this.components=(FormComponent<?>[])new FormComponent[] { formComponent1,formComponent2 };
    }
    public FormComponent<?>[] getDependentFormComponents(){
        return this.components;
    }
    public void validate(final Form<?> form){
        final FormComponent<?> formComponent1=this.components[0];
        final FormComponent<?> formComponent2=this.components[1];
        if(!Objects.equal((Object)formComponent1.getInput(),(Object)formComponent2.getInput())){
            this.error(formComponent2);
        }
    }
}
