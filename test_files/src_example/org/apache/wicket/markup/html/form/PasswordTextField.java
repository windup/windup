package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public class PasswordTextField extends TextField<String>{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private boolean resetPassword;
    public PasswordTextField(final String id){
        this(id,(IModel<String>)null);
    }
    public PasswordTextField(final String id,final IModel<String> model){
        super(id,model);
        this.setRequired(this.resetPassword=true);
        this.setType((Class<?>)String.class);
    }
    public final boolean getResetPassword(){
        return this.resetPassword;
    }
    public final PasswordTextField setResetPassword(final boolean resetPassword){
        this.resetPassword=resetPassword;
        return this;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(this.getResetPassword()){
            tag.put("value",(CharSequence)"");
        }
    }
    protected String getInputType(){
        return "password";
    }
    static{
        log=LoggerFactory.getLogger(PasswordTextField.class);
    }
}
