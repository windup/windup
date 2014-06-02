package org.apache.wicket.markup.html.tree;

import org.apache.wicket.model.*;
import javax.swing.tree.*;
import org.apache.wicket.*;

public class LabelTree extends BaseTree{
    private static final long serialVersionUID=1L;
    public LabelTree(final String id){
        super(id);
    }
    public LabelTree(final String id,final IModel<? extends TreeModel> model){
        super(id,model);
    }
    public LabelTree(final String id,final TreeModel model){
        super(id,new WicketTreeModel());
        this.setModelObject(model);
    }
    protected Component newNodeComponent(final String id,final IModel<Object> model){
        return new LabelIconPanel(id,model,this){
            private static final long serialVersionUID=1L;
            protected Component newContentComponent(final String componentId,final BaseTree tree,final IModel<?> model){
                return super.newContentComponent(componentId,tree,LabelTree.this.getNodeTextModel(model));
            }
        };
    }
    protected IModel<?> getNodeTextModel(final IModel<?> nodeModel){
        return nodeModel;
    }
}
