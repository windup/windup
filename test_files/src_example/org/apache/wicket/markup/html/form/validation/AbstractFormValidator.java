package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.validation.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.model.*;

public abstract class AbstractFormValidator extends Behavior implements IFormValidator{
    private static final long serialVersionUID=1L;
    public void error(final FormComponent<?> fc){
        this.error(fc,this.resourceKey(),this.variablesMap());
    }
    public void error(final FormComponent<?> fc,final String resourceKey){
        if(resourceKey==null){
            throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
        }
        this.error(fc,resourceKey,this.variablesMap());
    }
    public void error(final FormComponent<?> fc,final Map<String,Object> vars){
        if(vars==null){
            throw new IllegalArgumentException("Argument [[vars]] cannot be null");
        }
        this.error(fc,this.resourceKey(),vars);
    }
    public void error(final FormComponent<?> fc,final String resourceKey,final Map<String,Object> vars){
        if(fc==null){
            throw new IllegalArgumentException("Argument [[fc]] cannot be null");
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
        fc.error(error);
    }
    protected Map<String,Object> variablesMap(){
        final FormComponent<?>[] formComponents=this.getDependentFormComponents();
        if(formComponents!=null&&formComponents.length>0){
            final Map<String,Object> args=(Map<String,Object>)new HashMap(formComponents.length*3);
            for(int i=0;i<formComponents.length;++i){
                final FormComponent<?> formComponent=formComponents[i];
                final String arg="label"+i;
                final IModel<?> label=formComponent.getLabel();
                if(label!=null){
                    args.put(arg,label.getObject());
                }
                else{
                    args.put(arg,formComponent.getLocalizer().getString(formComponent.getId(),formComponent.getParent(),formComponent.getId()));
                }
                args.put("input"+i,formComponent.getInput());
                args.put("name"+i,formComponent.getId());
            }
            return args;
        }
        return (Map<String,Object>)new HashMap(2);
    }
    protected String resourceKey(){
        return Classes.simpleName(this.getClass());
    }
}
