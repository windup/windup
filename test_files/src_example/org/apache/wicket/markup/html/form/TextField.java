package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class TextField<T> extends AbstractTextComponent<T>{
    private static final long serialVersionUID=1L;
    public TextField(final String id){
        this(id,null,null);
    }
    public TextField(final String id,final Class<T> type){
        this(id,null,type);
    }
    public TextField(final String id,final IModel<T> model){
        this(id,model,null);
    }
    public TextField(final String id,final IModel<T> model,final Class<T> type){
        super(id,model);
        this.setType(type);
        this.setEscapeModelStrings(false);
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"input");
        final String inputType=this.getInputType();
        if(inputType!=null){
            this.checkComponentTagAttribute(tag,"type",inputType);
        }
        else if(tag.getAttributes().containsKey((Object)"type")){
            this.checkComponentTagAttribute(tag,"type","text");
        }
        tag.put("value",(CharSequence)this.getValue());
        super.onComponentTag(tag);
    }
    protected String getInputType(){
        return null;
    }
}
