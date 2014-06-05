package org.apache.wicket.markup.html.image;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.*;

public class ContextImage extends WebComponent{
    private static final long serialVersionUID=1L;
    public ContextImage(final String id,final IModel<String> contextRelativePath){
        super(id);
        this.add(new ContextPathGenerator(contextRelativePath));
    }
    public ContextImage(final String id,final String contextRelativePath){
        super(id);
        this.add(new ContextPathGenerator(contextRelativePath));
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"img");
        super.onComponentTag(tag);
    }
}
