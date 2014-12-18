package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;

public class CategoryNode extends DefaultMutableTreeNode{
    protected boolean _selected;
    protected int _numberOfContainedRecords;
    protected int _numberOfRecordsFromChildren;
    protected boolean _hasFatalChildren;
    protected boolean _hasFatalRecords;
    public CategoryNode(final String title){
        super();
        this._selected=true;
        this._numberOfContainedRecords=0;
        this._numberOfRecordsFromChildren=0;
        this._hasFatalChildren=false;
        this._hasFatalRecords=false;
        this.setUserObject(title);
    }
    public String getTitle(){
        return (String)this.getUserObject();
    }
    public void setSelected(final boolean s){
        if(s!=this._selected){
            this._selected=s;
        }
    }
    public boolean isSelected(){
        return this._selected;
    }
    public void setAllDescendantsSelected(){
        final Enumeration children=this.children();
        while(children.hasMoreElements()){
            final CategoryNode node=children.nextElement();
            node.setSelected(true);
            node.setAllDescendantsSelected();
        }
    }
    public void setAllDescendantsDeSelected(){
        final Enumeration children=this.children();
        while(children.hasMoreElements()){
            final CategoryNode node=children.nextElement();
            node.setSelected(false);
            node.setAllDescendantsDeSelected();
        }
    }
    public String toString(){
        return this.getTitle();
    }
    public boolean equals(final Object obj){
        if(obj instanceof CategoryNode){
            final CategoryNode node=(CategoryNode)obj;
            final String tit1=this.getTitle().toLowerCase();
            final String tit2=node.getTitle().toLowerCase();
            if(tit1.equals(tit2)){
                return true;
            }
        }
        return false;
    }
    public int hashCode(){
        return this.getTitle().hashCode();
    }
    public void addRecord(){
        ++this._numberOfContainedRecords;
        this.addRecordToParent();
    }
    public int getNumberOfContainedRecords(){
        return this._numberOfContainedRecords;
    }
    public void resetNumberOfContainedRecords(){
        this._numberOfContainedRecords=0;
        this._numberOfRecordsFromChildren=0;
        this._hasFatalRecords=false;
        this._hasFatalChildren=false;
    }
    public boolean hasFatalRecords(){
        return this._hasFatalRecords;
    }
    public boolean hasFatalChildren(){
        return this._hasFatalChildren;
    }
    public void setHasFatalRecords(final boolean flag){
        this._hasFatalRecords=flag;
    }
    public void setHasFatalChildren(final boolean flag){
        this._hasFatalChildren=flag;
    }
    protected int getTotalNumberOfRecords(){
        return this.getNumberOfRecordsFromChildren()+this.getNumberOfContainedRecords();
    }
    protected void addRecordFromChild(){
        ++this._numberOfRecordsFromChildren;
        this.addRecordToParent();
    }
    protected int getNumberOfRecordsFromChildren(){
        return this._numberOfRecordsFromChildren;
    }
    protected void addRecordToParent(){
        final TreeNode parent=this.getParent();
        if(parent==null){
            return;
        }
        ((CategoryNode)parent).addRecordFromChild();
    }
}
