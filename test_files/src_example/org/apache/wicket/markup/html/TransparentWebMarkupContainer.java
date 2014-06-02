package org.apache.wicket.markup.html;

import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public class TransparentWebMarkupContainer extends WebMarkupContainer implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public TransparentWebMarkupContainer(final String id){
        super(id);
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        final Component resolvedComponent=this.getParent().get(tag.getId());
        if(resolvedComponent!=null&&this.getPage().wasRendered(resolvedComponent)){
            return null;
        }
        return resolvedComponent;
    }
}
