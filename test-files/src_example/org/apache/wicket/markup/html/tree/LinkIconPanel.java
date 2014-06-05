package org.apache.wicket.markup.html.tree;

import org.apache.wicket.model.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.*;

public class LinkIconPanel extends LabelIconPanel{
    private static final long serialVersionUID=1L;
    public LinkIconPanel(final String id,final IModel<Object> model,final BaseTree tree){
        super(id,model,tree);
    }
    protected void addComponents(final IModel<Object> model,final BaseTree tree){
        final BaseTree.ILinkCallback callback=new BaseTree.ILinkCallback(){
            private static final long serialVersionUID=1L;
            public void onClick(final AjaxRequestTarget target){
                LinkIconPanel.this.onNodeLinkClicked(model.getObject(),tree,target);
            }
        };
        MarkupContainer link=tree.newLink("iconLink",callback);
        this.add(link);
        link.add(this.newImageComponent("icon",tree,model));
        link=tree.newLink("contentLink",callback);
        this.add(link);
        link.add(this.newContentComponent("content",tree,model));
    }
    protected void onNodeLinkClicked(final Object node,final BaseTree tree,final AjaxRequestTarget target){
        tree.getTreeState().selectNode(node,!tree.getTreeState().isNodeSelected(node));
        if(target!=null){
            tree.updateTree(target);
        }
    }
}
