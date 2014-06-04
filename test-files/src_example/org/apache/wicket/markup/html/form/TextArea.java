package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class TextArea<T> extends AbstractTextComponent<T>{
    private static final long serialVersionUID=1L;
    public TextArea(final String id){
        super(id);
    }
    public TextArea(final String id,final IModel<T> model){
        super(id,model);
    }
    public final void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        this.checkComponentTag(openTag,"textarea");
        String value=this.getValue();
        if(value!=null){
            if(value.startsWith("\n")){
                value="\n"+value;
            }
            else if(value.startsWith("\r\n")){
                value="\r\n"+value;
            }
            else if(value.startsWith("\r")){
                value="\r"+value;
            }
        }
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)value);
    }
}
