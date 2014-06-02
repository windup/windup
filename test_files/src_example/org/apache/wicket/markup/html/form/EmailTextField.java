package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.validation.validator.*;
import org.apache.wicket.validation.*;

public class EmailTextField extends TextField<String>{
    private static final long serialVersionUID=1L;
    public EmailTextField(final String id,final String emailAddress){
        this(id,new Model<String>(emailAddress));
    }
    public EmailTextField(final String id,final IModel<String> model){
        this(id,model,EmailAddressValidator.getInstance());
    }
    public EmailTextField(final String id){
        this(id,null,EmailAddressValidator.getInstance());
    }
    public EmailTextField(final String id,final IModel<String> model,final IValidator<String> emailValidator){
        super(id,model,(Class<String>)String.class);
        this.add(emailValidator);
    }
    protected String getInputType(){
        return "email";
    }
}
