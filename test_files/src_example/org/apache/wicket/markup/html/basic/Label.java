package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.*;

public class Label extends WebComponent{
    private static final long serialVersionUID=1L;
    public Label(final String id){
        super(id);
    }
    public Label(final String id,final String label){
        this(id,new Model<Object>(label));
    }
    public Label(final String id,final IModel<?> model){
        super(id,model);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)this.getDefaultModelObjectAsString());
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(tag.isOpenClose()){
            tag.setType(XmlTag.TagType.OPEN);
        }
    }
}
