package org.apache.wicket.markup.html.image;

import org.apache.wicket.behavior.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.string.*;

public class ContextPathGenerator extends Behavior{
    private static final long serialVersionUID=1L;
    private final IModel<String> contextRelativePath;
    public ContextPathGenerator(final IModel<String> contextRelativePath){
        super();
        this.contextRelativePath=contextRelativePath;
    }
    public ContextPathGenerator(final String contextRelativePath){
        super();
        this.contextRelativePath=new Model<String>(contextRelativePath);
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
        final String path=this.contextRelativePath.getObject();
        final String rewritten=UrlUtils.rewriteToContextRelative(path,RequestCycle.get());
        tag.put("src",(CharSequence)rewritten);
    }
    public void detach(final Component component){
        this.contextRelativePath.detach();
        super.detach(component);
    }
}
