package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.*;

public class FormComponentLabel extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private final LabeledWebMarkupContainer component;
    public FormComponentLabel(final String id,final LabeledWebMarkupContainer component){
        super(id);
        this.component=(LabeledWebMarkupContainer)Args.notNull((Object)component,"component");
        component.setOutputMarkupId(true);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        this.checkComponentTag(tag,"label");
        tag.put("for",(CharSequence)this.component.getMarkupId());
        tag.setType(XmlTag.TagType.OPEN);
    }
    public LabeledWebMarkupContainer getFormComponent(){
        return this.component;
    }
}
