package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.AWTEventMulticaster;
import javax.swing.tree.MutableTreeNode;
import java.util.Enumeration;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryElement;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.apache.log4j.lf5.LogRecord;
import javax.swing.tree.TreeNode;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.DefaultTreeModel;

public class CategoryExplorerModel extends DefaultTreeModel{
    protected boolean _renderFatal;
    protected ActionListener _listener;
    protected ActionEvent _event;
    public CategoryExplorerModel(final CategoryNode node){
        super(node);
        this._renderFatal=true;
        this._listener=null;
        this._event=new ActionEvent(this,1001,"Nodes Selection changed");
    }
    public void addLogRecord(final LogRecord lr){
        final CategoryPath path=new CategoryPath(lr.getCategory());
        this.addCategory(path);
        final CategoryNode node=this.getCategoryNode(path);
        node.addRecord();
        if(this._renderFatal&&lr.isFatal()){
            final TreeNode[] nodes=this.getPathToRoot(node);
            for(int len=nodes.length,i=1;i<len-1;++i){
                final CategoryNode parent=(CategoryNode)nodes[i];
                parent.setHasFatalChildren(true);
                this.nodeChanged(parent);
            }
            node.setHasFatalRecords(true);
            this.nodeChanged(node);
        }
    }
    public CategoryNode getRootCategoryNode(){
        return (CategoryNode)this.getRoot();
    }
    public CategoryNode getCategoryNode(final String category){
        final CategoryPath path=new CategoryPath(category);
        return this.getCategoryNode(path);
    }
    public CategoryNode getCategoryNode(final CategoryPath path){
        CategoryNode parent;
        final CategoryNode root=parent=(CategoryNode)this.getRoot();
        for(int i=0;i<path.size();++i){
            final CategoryElement element=path.categoryElementAt(i);
            final Enumeration children=parent.children();
            boolean categoryAlreadyExists=false;
            while(children.hasMoreElements()){
                final CategoryNode node=children.nextElement();
                final String title=node.getTitle().toLowerCase();
                final String pathLC=element.getTitle().toLowerCase();
                if(title.equals(pathLC)){
                    categoryAlreadyExists=true;
                    parent=node;
                    break;
                }
            }
            if(!categoryAlreadyExists){
                return null;
            }
        }
        return parent;
    }
    public boolean isCategoryPathActive(final CategoryPath path){
        CategoryNode parent;
        final CategoryNode root=parent=(CategoryNode)this.getRoot();
        boolean active=false;
        for(int i=0;i<path.size();++i){
            final CategoryElement element=path.categoryElementAt(i);
            final Enumeration children=parent.children();
            boolean categoryAlreadyExists=false;
            active=false;
            while(children.hasMoreElements()){
                final CategoryNode node=children.nextElement();
                final String title=node.getTitle().toLowerCase();
                final String pathLC=element.getTitle().toLowerCase();
                if(title.equals(pathLC)){
                    categoryAlreadyExists=true;
                    parent=node;
                    if(parent.isSelected()){
                        active=true;
                        break;
                    }
                    break;
                }
            }
            if(!active||!categoryAlreadyExists){
                return false;
            }
        }
        return active;
    }
    public CategoryNode addCategory(final CategoryPath path){
        CategoryNode parent;
        final CategoryNode root=parent=(CategoryNode)this.getRoot();
        for(int i=0;i<path.size();++i){
            final CategoryElement element=path.categoryElementAt(i);
            final Enumeration children=parent.children();
            boolean categoryAlreadyExists=false;
            while(children.hasMoreElements()){
                final CategoryNode node=children.nextElement();
                final String title=node.getTitle().toLowerCase();
                final String pathLC=element.getTitle().toLowerCase();
                if(title.equals(pathLC)){
                    categoryAlreadyExists=true;
                    parent=node;
                    break;
                }
            }
            if(!categoryAlreadyExists){
                final CategoryNode newNode=new CategoryNode(element.getTitle());
                this.insertNodeInto(newNode,parent,parent.getChildCount());
                this.refresh(newNode);
                parent=newNode;
            }
        }
        return parent;
    }
    public void update(final CategoryNode node,final boolean selected){
        if(node.isSelected()==selected){
            return;
        }
        if(selected){
            this.setParentSelection(node,true);
        }
        else{
            this.setDescendantSelection(node,false);
        }
    }
    public void setDescendantSelection(final CategoryNode node,final boolean selected){
        final Enumeration descendants=node.depthFirstEnumeration();
        while(descendants.hasMoreElements()){
            final CategoryNode current=descendants.nextElement();
            if(current.isSelected()!=selected){
                current.setSelected(selected);
                this.nodeChanged(current);
            }
        }
        this.notifyActionListeners();
    }
    public void setParentSelection(final CategoryNode node,final boolean selected){
        final TreeNode[] nodes=this.getPathToRoot(node);
        for(int len=nodes.length,i=1;i<len;++i){
            final CategoryNode parent=(CategoryNode)nodes[i];
            if(parent.isSelected()!=selected){
                parent.setSelected(selected);
                this.nodeChanged(parent);
            }
        }
        this.notifyActionListeners();
    }
    public synchronized void addActionListener(final ActionListener l){
        this._listener=AWTEventMulticaster.add(this._listener,l);
    }
    public synchronized void removeActionListener(final ActionListener l){
        this._listener=AWTEventMulticaster.remove(this._listener,l);
    }
    public void resetAllNodeCounts(){
        final Enumeration nodes=this.getRootCategoryNode().depthFirstEnumeration();
        while(nodes.hasMoreElements()){
            final CategoryNode current=nodes.nextElement();
            current.resetNumberOfContainedRecords();
            this.nodeChanged(current);
        }
    }
    public TreePath getTreePathToRoot(final CategoryNode node){
        if(node==null){
            return null;
        }
        return new TreePath(this.getPathToRoot(node));
    }
    protected void notifyActionListeners(){
        if(this._listener!=null){
            this._listener.actionPerformed(this._event);
        }
    }
    protected void refresh(final CategoryNode node){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                CategoryExplorerModel.this.nodeChanged(node);
            }
        });
    }
}
