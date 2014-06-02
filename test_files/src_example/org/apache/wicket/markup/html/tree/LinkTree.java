package org.apache.wicket.markup.html.tree;

import org.apache.wicket.model.*;
import javax.swing.tree.*;
import org.apache.wicket.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.html.basic.*;

public class LinkTree extends LabelTree{
    private static final long serialVersionUID=1L;
    public LinkTree(final String id){
        super(id);
    }
    public LinkTree(final String id,final IModel<? extends TreeModel> model){
        super(id,model);
    }
    public LinkTree(final String id,final TreeModel model){
        super(id,new WicketTreeModel());
        this.setModelObject(model);
    }
    protected Component newNodeComponent(final String id,final IModel<Object> model){
        return new LinkIconPanel(id,model,this){
            private static final long serialVersionUID=1L;
            protected void onNodeLinkClicked(final Object node,final BaseTree tree,final AjaxRequestTarget target){
                super.onNodeLinkClicked(node,tree,target);
                LinkTree.this.onNodeLinkClicked(node,tree,target);
            }
            protected Component newContentComponent(final String componentId,final BaseTree tree,final IModel<?> model){
                return new Label(componentId,LinkTree.this.getNodeTextModel(model));
            }
        };
    }
    protected void onNodeLinkClicked(final Object node,final BaseTree tree,final AjaxRequestTarget target){
    }
}
