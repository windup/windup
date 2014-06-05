package org.apache.wicket.markup.html.tree;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.util.string.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.markup.html.list.*;
import java.io.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.request.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.markup.*;

public abstract class AbstractTree extends Panel implements ITreeStateListener,TreeModelListener,AjaxRequestTarget.ITargetRespondListener{
    private static final long serialVersionUID=1L;
    private boolean attached;
    private final AppendingStringBuffer deleteIds;
    private boolean dirtyAll;
    private final Set<TreeItem> dirtyItems;
    private final Set<TreeItem> dirtyItemsCreateDOM;
    private int idCounter;
    private TreeItemContainer itemContainer;
    private final Map<Object,TreeItem> nodeToItemMap;
    private TreeModel previousModel;
    private TreeItem rootItem;
    private boolean rootLess;
    private ITreeState state;
    private static final ResourceReference JAVASCRIPT;
    public AbstractTree(final String id){
        super(id);
        this.attached=false;
        this.deleteIds=new AppendingStringBuffer();
        this.dirtyAll=false;
        this.dirtyItems=(Set<TreeItem>)new HashSet();
        this.dirtyItemsCreateDOM=(Set<TreeItem>)new HashSet();
        this.idCounter=0;
        this.nodeToItemMap=(Map<Object,TreeItem>)new HashMap();
        this.previousModel=null;
        this.rootItem=null;
        this.rootLess=false;
        this.init();
    }
    public AbstractTree(final String id,final IModel<? extends TreeModel> model){
        super(id,model);
        this.attached=false;
        this.deleteIds=new AppendingStringBuffer();
        this.dirtyAll=false;
        this.dirtyItems=(Set<TreeItem>)new HashSet();
        this.dirtyItemsCreateDOM=(Set<TreeItem>)new HashSet();
        this.idCounter=0;
        this.nodeToItemMap=(Map<Object,TreeItem>)new HashMap();
        this.previousModel=null;
        this.rootItem=null;
        this.rootLess=false;
        this.init();
    }
    public final void allNodesCollapsed(){
        this.invalidateAll();
    }
    public final void allNodesExpanded(){
        this.invalidateAll();
    }
    public IModel<? extends TreeModel> getModel(){
        return (IModel<? extends TreeModel>)this.getDefaultModel();
    }
    public TreeModel getModelObject(){
        return (TreeModel)this.getDefaultModelObject();
    }
    public MarkupContainer setModel(final IModel<? extends TreeModel> model){
        this.setDefaultModel(model);
        return this;
    }
    public MarkupContainer setModelObject(final TreeModel model){
        this.setDefaultModelObject(model);
        return this;
    }
    public ITreeState getTreeState(){
        if(this.state==null){
            (this.state=this.newTreeState()).addTreeStateListener(this);
        }
        return this.state;
    }
    protected void onBeforeAttach(){
    }
    private void onBeforeRenderInternal(){
        if(!this.attached){
            this.onBeforeAttach();
            this.checkModel();
            if(this.dirtyAll&&this.rootItem!=null){
                this.clearAllItem();
            }
            else{
                this.rebuildDirty();
            }
            if(this.rootItem==null){
                final Object rootNode=this.getModelObject().getRoot();
                if(rootNode!=null){
                    if(this.isRootLess()){
                        this.rootItem=this.newTreeItem(null,rootNode,-1);
                    }
                    else{
                        this.rootItem=this.newTreeItem(null,rootNode,0);
                    }
                    this.itemContainer.add(this.rootItem);
                    this.buildItemChildren(this.rootItem);
                }
            }
            this.attached=true;
        }
    }
    public void onBeforeRender(){
        this.onBeforeRenderInternal();
        super.onBeforeRender();
    }
    public void onDetach(){
        this.attached=false;
        super.onDetach();
        if(this.getTreeState() instanceof IDetachable){
            ((IDetachable)this.getTreeState()).detach();
        }
    }
    public final void invalidateAll(){
        this.updated();
        this.dirtyAll=true;
    }
    public final boolean isRootLess(){
        return this.rootLess;
    }
    public final void nodeCollapsed(final Object node){
        if(this.isNodeVisible(node)){
            this.invalidateNodeWithChildren(node);
        }
    }
    public final void nodeExpanded(final Object node){
        if(this.isNodeVisible(node)){
            this.invalidateNodeWithChildren(node);
        }
    }
    public final void nodeSelected(final Object node){
        if(this.isNodeVisible(node)){
            this.invalidateNode(node,this.isForceRebuildOnSelectionChange());
        }
    }
    public final void nodeUnselected(final Object node){
        if(this.isNodeVisible(node)){
            this.invalidateNode(node,this.isForceRebuildOnSelectionChange());
        }
    }
    protected boolean isForceRebuildOnSelectionChange(){
        return true;
    }
    public void setRootLess(final boolean rootLess){
        if(this.rootLess!=rootLess){
            this.rootLess=rootLess;
            this.invalidateAll();
            if(rootLess&&this.getModelObject()!=null){
                this.getTreeState().expandNode(this.getModelObject().getRoot());
            }
        }
    }
    public final void treeNodesChanged(final TreeModelEvent e){
        if(this.dirtyAll){
            return;
        }
        if(e.getChildren()==null){
            if(this.rootItem!=null){
                this.invalidateNode(this.rootItem.getModelObject(),true);
            }
        }
        else{
            final Object[] children=e.getChildren();
            if(children!=null){
                for(final Object node : children){
                    if(this.isNodeVisible(node)){
                        this.invalidateNode(node,true);
                    }
                }
            }
        }
    }
    private void markTheLastButOneChildDirty(final TreeItem parent,final TreeItem child){
        if(parent.getChildren().indexOf(child)==parent.getChildren().size()-1){
            for(int i=parent.getChildren().size()-2;i>=0;--i){
                final TreeItem item=(TreeItem)parent.getChildren().get(i);
                this.invalidateNodeWithChildren(item.getModelObject());
            }
        }
    }
    public final void treeNodesInserted(final TreeModelEvent e){
        if(this.dirtyAll){
            return;
        }
        final Object parentNode=e.getTreePath().getLastPathComponent();
        final TreeItem parentItem=(TreeItem)this.nodeToItemMap.get(parentNode);
        if(parentItem!=null&&this.isNodeVisible(parentNode)){
            final List<?> eventChildren=(List<?>)Arrays.asList(e.getChildren());
            boolean wasLeaf=true;
            for(int nodeChildCount=this.getChildCount(parentNode),i=0;wasLeaf&&i<nodeChildCount;wasLeaf=eventChildren.contains(this.getChildAt(parentNode,i)),++i){
            }
            final boolean addingToHiddedRoot=parentItem.getParentItem()==null&&this.isRootLess();
            if(wasLeaf&&!addingToHiddedRoot){
                final Object grandparentNode=this.getParentNode(parentNode);
                final boolean addingToHiddedRootSon=grandparentNode!=null&&this.getParentNode(grandparentNode)==null&&this.isRootLess();
                if(grandparentNode!=null&&!addingToHiddedRootSon){
                    this.invalidateNodeWithChildren(grandparentNode);
                }
                else{
                    this.invalidateNode(parentNode,true);
                }
                this.getTreeState().expandNode(parentNode);
            }
            else if(this.isNodeExpanded(parentNode)){
                final List<TreeItem> itemChildren=parentItem.getChildren();
                final int childLevel=parentItem.getLevel()+1;
                final int[] childIndices=e.getChildIndices();
                for(int j=0;j<eventChildren.size();++j){
                    final TreeItem item=this.newTreeItem(parentItem,eventChildren.get(j),childLevel);
                    this.itemContainer.add(item);
                    if(itemChildren!=null){
                        itemChildren.add(childIndices[j],item);
                        this.markTheLastButOneChildDirty(parentItem,item);
                    }
                    if(!this.dirtyItems.contains(item)){
                        this.dirtyItems.add(item);
                    }
                    if(!this.dirtyItemsCreateDOM.contains(item)&&!item.hasParentWithChildrenMarkedToRecreation()){
                        this.dirtyItemsCreateDOM.add(item);
                    }
                }
            }
        }
    }
    public final void treeNodesRemoved(final TreeModelEvent removalEvent){
        if(this.dirtyAll){
            return;
        }
        final Object parentNode=removalEvent.getTreePath().getLastPathComponent();
        final TreeItem parentItem=(TreeItem)this.nodeToItemMap.get(parentNode);
        final List<Object> selection=(List<Object>)new ArrayList(this.getTreeState().getSelectedNodes());
        final List<Object> removed=(List<Object>)Arrays.asList(removalEvent.getChildren());
        for(Object cursor : selection){
            final Object selectedNode=cursor;
            while(cursor!=null){
                if(removed.contains(cursor)){
                    this.getTreeState().selectNode(selectedNode,false);
                }
                if(cursor instanceof TreeNode){
                    cursor=((TreeNode)cursor).getParent();
                }
                else{
                    cursor=null;
                }
            }
        }
        if(parentItem!=null&&this.isNodeVisible(parentNode)){
            if(this.isNodeExpanded(parentNode)){
                for(final Object deletedNode : removalEvent.getChildren()){
                    final TreeItem itemToDelete=(TreeItem)this.nodeToItemMap.get(deletedNode);
                    if(itemToDelete!=null){
                        this.markTheLastButOneChildDirty(parentItem,itemToDelete);
                        this.visitItemChildren(itemToDelete,new IItemCallback(){
                            public void visitItem(final TreeItem item){
                                AbstractTree.this.removeItem(item);
                            }
                        });
                        parentItem.getChildren().remove(itemToDelete);
                        this.removeItem(itemToDelete);
                    }
                }
            }
            if(!parentItem.hasChildTreeItems()){
                this.invalidateNode(parentNode,true);
            }
        }
    }
    public final void treeStructureChanged(final TreeModelEvent e){
        if(this.dirtyAll){
            return;
        }
        final Object node=(e.getTreePath()!=null)?e.getTreePath().getLastPathComponent():null;
        if(node==null||e.getTreePath().getPathCount()==1){
            this.invalidateAll();
        }
        else{
            this.invalidateNodeWithChildren(node);
        }
    }
    protected void addComponent(final AjaxRequestTarget target,final Component component){
        target.add(component);
    }
    public void onTargetRespond(final AjaxRequestTarget target){
        this.checkModel();
        if(this.dirtyAll){
            this.addComponent(target,this);
        }
        else{
            if(this.deleteIds.length()!=0){
                final String js=this.getElementsDeleteJavaScript();
                target.prependJavaScript((CharSequence)js);
            }
            while(!this.dirtyItemsCreateDOM.isEmpty()){
                final Iterator<TreeItem> i=(Iterator<TreeItem>)this.dirtyItemsCreateDOM.iterator();
                while(i.hasNext()){
                    final TreeItem item=(TreeItem)i.next();
                    final TreeItem parent=item.getParentItem();
                    final int index=parent.getChildren().indexOf(item);
                    TreeItem previous;
                    if(index==0){
                        previous=parent;
                    }
                    else{
                        for(previous=(TreeItem)parent.getChildren().get(index-1);previous.getChildren()!=null&&previous.getChildren().size()>0;previous=(TreeItem)previous.getChildren().get(previous.getChildren().size()-1)){
                        }
                    }
                    if(!this.dirtyItemsCreateDOM.contains(previous)){
                        target.prependJavaScript((CharSequence)("Wicket.Tree.createElement(\""+item.getMarkupId()+"\","+"\""+previous.getMarkupId()+"\")"));
                        i.remove();
                    }
                }
            }
            final Iterator i$=this.dirtyItems.iterator();
            while(i$.hasNext()){
                final TreeItem item=(TreeItem)i$.next();
                if(item.getChildren()==null){
                    this.buildItemChildren(item);
                    item.setRenderChildren(true);
                }
                this.addComponent(target,item);
            }
            this.updated();
        }
    }
    public final void updateTree(){
        final AjaxRequestTarget handler=AjaxRequestTarget.get();
        if(handler==null){
            throw new WicketRuntimeException("No AjaxRequestTarget available to execute updateTree(ART target)");
        }
        this.updateTree(handler);
    }
    public final void updateTree(final AjaxRequestTarget target){
        Args.notNull((Object)target,"target");
        target.registerRespondListener(this);
    }
    protected final boolean isNodeExpanded(final Object node){
        return (this.isRootLess()&&this.rootItem!=null&&this.rootItem.getModelObject().equals(node))||this.getTreeState().isNodeExpanded(node);
    }
    protected ITreeState newTreeState(){
        return new DefaultTreeState();
    }
    protected void onAfterRender(){
        super.onAfterRender();
        this.updated();
    }
    protected abstract void populateTreeItem(final WebMarkupContainer p0,final int p1);
    private void buildItemChildren(final TreeItem item){
        List<TreeItem> items;
        if(this.isNodeExpanded(item.getModelObject())){
            items=this.buildTreeItems(item,this.nodeChildren(item.getModelObject()),item.getLevel()+1);
        }
        else{
            items=(List<TreeItem>)new ArrayList(0);
        }
        item.setChildren(items);
    }
    private List<TreeItem> buildTreeItems(final TreeItem parent,final Iterator<Object> nodes,final int level){
        final List<TreeItem> result=(List<TreeItem>)new ArrayList();
        while(nodes.hasNext()){
            final Object node=nodes.next();
            final TreeItem item=this.newTreeItem(parent,node,level);
            this.itemContainer.add(item);
            this.buildItemChildren(item);
            result.add(item);
        }
        return result;
    }
    private void checkModel(){
        final TreeModel model=this.getModelObject();
        if(model!=this.previousModel){
            if(this.previousModel!=null){
                this.previousModel.removeTreeModelListener(this);
            }
            if((this.previousModel=model)!=null){
                model.addTreeModelListener(this);
            }
            this.invalidateAll();
        }
    }
    private void clearAllItem(){
        this.visitItemAndChildren(this.rootItem,new IItemCallback(){
            public void visitItem(final TreeItem item){
                item.remove();
            }
        });
        this.rootItem=null;
    }
    private String getElementsDeleteJavaScript(){
        final AppendingStringBuffer buffer=new AppendingStringBuffer(100);
        buffer.append("Wicket.Tree.removeNodes(\"");
        buffer.append(this.getMarkupId()+"_\",[");
        buffer.append(this.deleteIds);
        if(buffer.endsWith((CharSequence)",")){
            buffer.setLength(buffer.length()-1);
        }
        buffer.append("]);");
        return buffer.toString();
    }
    private String getShortItemId(final TreeItem item){
        final int skip=this.getMarkupId().length()+1;
        return item.getMarkupId().substring(skip);
    }
    private void init(){
        this.setVersioned(false);
        this.setOutputMarkupId(true);
        this.itemContainer=new TreeItemContainer("i");
        this.add(this.itemContainer);
        this.checkModel();
    }
    public final void markNodeDirty(final Object node){
        this.invalidateNode(node,false);
    }
    public final void markNodeChildrenDirty(final Object node){
        final TreeItem item=(TreeItem)this.nodeToItemMap.get(node);
        if(item!=null){
            this.visitItemChildren(item,new IItemCallback(){
                public void visitItem(final TreeItem item){
                    AbstractTree.this.invalidateNode(item.getModelObject(),false);
                }
            });
        }
    }
    private void invalidateNode(final Object node,final boolean forceRebuild){
        if(!this.dirtyAll){
            TreeItem item=(TreeItem)this.nodeToItemMap.get(node);
            if(item!=null){
                boolean createDOM=false;
                if(forceRebuild){
                    final int level=item.getLevel();
                    final List<TreeItem> children=item.getChildren();
                    final String id=item.getId();
                    final TreeItem parent=item.getParentItem();
                    final int index=(parent!=null)?parent.getChildren().indexOf(item):-1;
                    createDOM=this.dirtyItemsCreateDOM.contains(item);
                    this.dirtyItems.remove(item);
                    this.dirtyItemsCreateDOM.remove(item);
                    item.remove();
                    item=this.newTreeItem(parent,node,level,id);
                    this.itemContainer.add(item);
                    item.setChildren(children);
                    if(parent==null){
                        this.rootItem=item;
                    }
                    else{
                        parent.getChildren().set(index,item);
                    }
                }
                if(!this.dirtyItems.contains(item)){
                    this.dirtyItems.add(item);
                }
                if(createDOM&&!this.dirtyItemsCreateDOM.contains(item)){
                    this.dirtyItemsCreateDOM.add(item);
                }
            }
        }
    }
    private void invalidateNodeWithChildren(final Object node){
        if(!this.dirtyAll){
            final TreeItem item=(TreeItem)this.nodeToItemMap.get(node);
            if(item!=null){
                this.visitItemChildren(item,new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        AbstractTree.this.removeItem(item);
                    }
                });
                item.setChildren(null);
                if(!this.dirtyItems.contains(item)){
                    this.dirtyItems.add(item);
                }
            }
        }
    }
    private boolean isNodeVisible(final Object node){
        if(node==null){
            return false;
        }
        for(Object parent=this.getParentNode(node);parent!=null;parent=this.getParentNode(parent)){
            if(!this.isNodeExpanded(parent)){
                return false;
            }
        }
        return true;
    }
    public Object getParentNode(final Object node){
        final TreeItem item=(TreeItem)this.nodeToItemMap.get(node);
        if(item==null){
            return null;
        }
        final TreeItem parent=item.getParentItem();
        return (parent==null)?null:parent.getModelObject();
    }
    private TreeItem newTreeItem(final TreeItem parent,final Object node,final int level){
        return new TreeItem(parent,""+this.idCounter++,node,level);
    }
    private TreeItem newTreeItem(final TreeItem parent,final Object node,final int level,final String id){
        return new TreeItem(parent,id,node,level);
    }
    public final Iterator<Object> nodeChildren(final Object node){
        final TreeModel model=this.getTreeModel();
        final int count=model.getChildCount(node);
        final List<Object> nodes=(List<Object>)new ArrayList(count);
        for(int i=0;i<count;++i){
            nodes.add(model.getChild(node,i));
        }
        return (Iterator<Object>)nodes.iterator();
    }
    public final Object getChildAt(final Object parent,final int index){
        return this.getTreeModel().getChild(parent,index);
    }
    public final boolean isLeaf(final Object node){
        return this.getTreeModel().isLeaf(node);
    }
    public final int getChildCount(final Object parent){
        return this.getTreeModel().getChildCount(parent);
    }
    private TreeModel getTreeModel(){
        return this.getModelObject();
    }
    private void rebuildDirty(){
        for(final TreeItem item : this.dirtyItems){
            if(item.getChildren()==null){
                this.buildItemChildren(item);
            }
        }
    }
    private void removeItem(final TreeItem item){
        this.dirtyItems.remove(item);
        if(this.dirtyItemsCreateDOM.contains(item)){
            this.dirtyItemsCreateDOM.remove(item);
        }
        else{
            this.deleteIds.append(this.getShortItemId(item));
            this.deleteIds.append(",");
        }
        if(item.getParent()!=null){
            item.remove();
        }
    }
    private void updated(){
        this.dirtyAll=false;
        this.dirtyItems.clear();
        this.dirtyItemsCreateDOM.clear();
        this.deleteIds.clear();
    }
    private void visitItemAndChildren(final TreeItem item,final IItemCallback callback){
        callback.visitItem(item);
        this.visitItemChildren(item,callback);
    }
    private void visitItemChildren(final TreeItem item,final IItemCallback callback){
        if(item.getChildren()!=null){
            for(final TreeItem child : item.getChildren()){
                this.visitItemAndChildren(child,callback);
            }
        }
    }
    public Component getNodeComponent(final Object node){
        return (Component)this.nodeToItemMap.get(node);
    }
    public void renderHead(final IHeaderResponse response){
        response.renderJavaScriptReference(AbstractTree.JAVASCRIPT);
    }
    static{
        JAVASCRIPT=new JavaScriptResourceReference((Class<?>)AbstractTree.class,"res/tree.js");
    }
    private final class TreeItem extends AbstractItem{
        private static final int FLAG_RENDER_CHILDREN=524288;
        private static final long serialVersionUID=1L;
        private List<TreeItem> children;
        private final int level;
        private final TreeItem parent;
        public TreeItem(final TreeItem parent,final String id,final Object node,final int level){
            super(id,new Model<Object>(node));
            this.children=null;
            this.parent=parent;
            AbstractTree.this.nodeToItemMap.put(node,this);
            this.level=level;
            this.setOutputMarkupId(true);
            if(level!=-1){
                AbstractTree.this.populateTreeItem(this,level);
            }
        }
        public TreeItem getParentItem(){
            return this.parent;
        }
        public List<TreeItem> getChildren(){
            return this.children;
        }
        public int getLevel(){
            return this.level;
        }
        public String getMarkupId(){
            return AbstractTree.this.getMarkupId()+"_"+this.getId();
        }
        public void setChildren(final List<TreeItem> children){
            this.children=children;
        }
        protected final boolean isRenderChildren(){
            return this.getFlag(524288);
        }
        public boolean hasChildTreeItems(){
            return this.children!=null&&!this.children.isEmpty();
        }
        protected void onRender(){
            if(this==AbstractTree.this.rootItem&&AbstractTree.this.isRootLess()){
                final String tagName=((ComponentTag)this.getMarkup().get(0)).getName();
                final Response response=this.getResponse();
                response.write((CharSequence)("<"+tagName+" style=\"display:none\" id=\""+this.getMarkupId()+"\">"));
                if("table".equals(tagName)){
                    response.write((CharSequence)"<tbody><tr><td></td></tr></tbody>");
                }
                response.write((CharSequence)("</"+tagName+">"));
            }
            else{
                super.onRender();
                if(this.isRenderChildren()){
                    AbstractTree.this.visitItemChildren(this,new IItemCallback(){
                        public void visitItem(final TreeItem item){
                            item.onRender();
                            final List<? extends Behavior> behaviors=item.getBehaviors();
                            for(final Behavior behavior : behaviors){
                                behavior.afterRender(item);
                            }
                        }
                    });
                }
            }
        }
        public Object getModelObject(){
            return this.getDefaultModelObject();
        }
        public void renderHead(final HtmlHeaderContainer container){
            super.renderHead(container);
            if(this.isRenderChildren()){
                AbstractTree.this.visitItemChildren(this,new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        item.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                            public void component(final Component component,final IVisit<Void> visit){
                                if(component.isVisible()){
                                    component.renderHead(container);
                                }
                                else{
                                    visit.dontGoDeeper();
                                }
                            }
                        });
                    }
                });
            }
        }
        protected final void setRenderChildren(final boolean value){
            this.setFlag(524288,value);
        }
        protected void onDetach(){
            super.onDetach();
            final Object object=this.getModelObject();
            if(object instanceof IDetachable){
                ((IDetachable)object).detach();
            }
            if(this.isRenderChildren()){
                AbstractTree.this.visitItemChildren(this,new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        item.detach();
                    }
                });
            }
            this.setRenderChildren(false);
        }
        protected void onBeforeRender(){
            AbstractTree.this.onBeforeRenderInternal();
            super.onBeforeRender();
            if(this.isRenderChildren()){
                AbstractTree.this.visitItemChildren(this,new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        item.prepareForRender();
                    }
                });
            }
        }
        protected void onAfterRender(){
            super.onAfterRender();
            if(this.isRenderChildren()){
                AbstractTree.this.visitItemChildren(this,new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        item.afterRender();
                    }
                });
            }
        }
        private boolean hasParentWithChildrenMarkedToRecreation(){
            return this.getParentItem()!=null&&(this.getParentItem().getChildren()==null||this.getParentItem().hasParentWithChildrenMarkedToRecreation());
        }
    }
    private class TreeItemContainer extends WebMarkupContainer{
        private static final long serialVersionUID=1L;
        public TreeItemContainer(final String id){
            super(id);
        }
        public TreeItemContainer remove(final Component component){
            if(component instanceof TreeItem){
                AbstractTree.this.nodeToItemMap.remove(((TreeItem)component).getModelObject());
            }
            super.remove(component);
            return this;
        }
        protected void onRender(){
            if(AbstractTree.this.rootItem!=null){
                final IItemCallback callback=new IItemCallback(){
                    public void visitItem(final TreeItem item){
                        item.render();
                    }
                };
                AbstractTree.this.visitItemAndChildren(AbstractTree.this.rootItem,callback);
            }
        }
        public IMarkupFragment getMarkup(final Component child){
            return this.getMarkup();
        }
    }
    private interface IItemCallback{
        void visitItem(TreeItem p0);
    }
}
