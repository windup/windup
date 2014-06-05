package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.parser.*;

public class MultiLineLabel extends WebComponent{
    private static final long serialVersionUID=1L;
    public MultiLineLabel(final String id){
        super(id);
    }
    public MultiLineLabel(final String id,final String label){
        this(id,new Model<Object>(label));
    }
    public MultiLineLabel(final String id,final IModel<?> model){
        super(id,model);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final CharSequence body=Strings.toMultilineMarkup((CharSequence)this.getDefaultModelObjectAsString());
        this.replaceComponentTagBody(markupStream,openTag,body);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.setType(XmlTag.TagType.OPEN);
    }
}
