package org.apache.wicket.markup.html.tree;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.image.*;
import org.apache.wicket.markup.html.basic.*;
import javax.swing.tree.*;
import org.apache.wicket.request.resource.*;

public class LabelIconPanel extends Panel{
    private static final long serialVersionUID=1L;
    private static final ResourceReference RESOURCE_FOLDER_OPEN;
    private static final ResourceReference RESOURCE_FOLDER_CLOSED;
    private static final ResourceReference RESOURCE_ITEM;
    public LabelIconPanel(final String id,final IModel<Object> model,final BaseTree tree){
        super(id,model);
        this.addComponents(model,tree);
    }
    protected void addComponents(final IModel<Object> model,final BaseTree tree){
        this.add(this.newImageComponent("icon",tree,model));
        this.add(this.newContentComponent("content",tree,model));
    }
    protected Component newImageComponent(final String componentId,final BaseTree tree,final IModel<Object> model){
        return new Image(componentId){
            private static final long serialVersionUID=1L;
            protected ResourceReference getImageResourceReference(){
                return LabelIconPanel.this.getImageResourceReference(tree,model.getObject());
            }
            protected boolean shouldAddAntiCacheParameter(){
                return false;
            }
        };
    }
    protected Component newContentComponent(final String componentId,final BaseTree tree,final IModel<?> model){
        return new Label(componentId,model);
    }
    protected ResourceReference getImageResourceReference(final BaseTree tree,final Object node){
        final TreeModel model=(TreeModel)tree.getDefaultModelObject();
        if(model.isLeaf(node)){
            return this.getResourceItemLeaf(node);
        }
        if(tree.getTreeState().isNodeExpanded(node)){
            return this.getResourceFolderOpen(node);
        }
        return this.getResourceFolderClosed(node);
    }
    protected IModel<Object> wrapNodeModel(final IModel<Object> nodeModel){
        return nodeModel;
    }
    protected ResourceReference getResourceFolderClosed(final Object node){
        return LabelIconPanel.RESOURCE_FOLDER_CLOSED;
    }
    protected ResourceReference getResourceFolderOpen(final Object node){
        return LabelIconPanel.RESOURCE_FOLDER_OPEN;
    }
    protected ResourceReference getResourceItemLeaf(final Object node){
        return LabelIconPanel.RESOURCE_ITEM;
    }
    static{
        RESOURCE_FOLDER_OPEN=new PackageResourceReference((Class<?>)LabelIconPanel.class,"res/folder-open.gif");
        RESOURCE_FOLDER_CLOSED=new PackageResourceReference((Class<?>)LabelIconPanel.class,"res/folder-closed.gif");
        RESOURCE_ITEM=new PackageResourceReference((Class<?>)LabelIconPanel.class,"res/item.gif");
    }
}
