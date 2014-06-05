package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.validation.validator.*;
import org.apache.wicket.validation.*;

public class UrlTextField extends TextField<String>{
    private static final long serialVersionUID=1L;
    public UrlTextField(final String id,final String url){
        this(id,new Model<String>(url));
    }
    public UrlTextField(final String id,final IModel<String> model){
        this(id,model,new UrlValidator());
    }
    public UrlTextField(final String id,final IModel<String> model,final UrlValidator urlValidator){
        super(id,model,(Class<String>)String.class);
        this.add(urlValidator);
    }
    protected String getInputType(){
        return "url";
    }
}
