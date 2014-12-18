package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.tree.TreePath;
import javax.swing.tree.MutableTreeNode;
import java.util.Enumeration;
import javax.swing.JPopupMenu;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTree;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryExplorerModel;
import javax.swing.JCheckBox;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNode;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNodeEditorRenderer;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryAbstractCellEditor;

public class CategoryNodeEditor extends CategoryAbstractCellEditor{
    protected CategoryNodeEditorRenderer _renderer;
    protected CategoryNode _lastEditedNode;
    protected JCheckBox _checkBox;
    protected CategoryExplorerModel _categoryModel;
    protected JTree _tree;
    public CategoryNodeEditor(final CategoryExplorerModel model){
        super();
        this._renderer=new CategoryNodeEditorRenderer();
        this._checkBox=this._renderer.getCheckBox();
        this._categoryModel=model;
        this._checkBox.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this._categoryModel.update(CategoryNodeEditor.this._lastEditedNode,CategoryNodeEditor.this._checkBox.isSelected());
                CategoryNodeEditor.this.stopCellEditing();
            }
        });
        this._renderer.addMouseListener(new MouseAdapter(){
            public void mousePressed(final MouseEvent e){
                if((e.getModifiers()&0x4)!=0x0){
                    CategoryNodeEditor.this.showPopup(CategoryNodeEditor.this._lastEditedNode,e.getX(),e.getY());
                }
                CategoryNodeEditor.this.stopCellEditing();
            }
        });
    }
    public Component getTreeCellEditorComponent(final JTree tree,final Object value,final boolean selected,final boolean expanded,final boolean leaf,final int row){
        this._lastEditedNode=(CategoryNode)value;
        this._tree=tree;
        return this._renderer.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,true);
    }
    public Object getCellEditorValue(){
        return this._lastEditedNode.getUserObject();
    }
    protected JMenuItem createPropertiesMenuItem(final CategoryNode node){
        final JMenuItem result=new JMenuItem("Properties");
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this.showPropertiesDialog(node);
            }
        });
        return result;
    }
    protected void showPropertiesDialog(final CategoryNode node){
        JOptionPane.showMessageDialog(this._tree,this.getDisplayedProperties(node),"Category Properties: "+node.getTitle(),-1);
    }
    protected Object getDisplayedProperties(final CategoryNode node){
        final ArrayList result=new ArrayList();
        result.add("Category: "+node.getTitle());
        if(node.hasFatalRecords()){
            result.add("Contains at least one fatal LogRecord.");
        }
        if(node.hasFatalChildren()){
            result.add("Contains descendants with a fatal LogRecord.");
        }
        result.add("LogRecords in this category alone: "+node.getNumberOfContainedRecords());
        result.add("LogRecords in descendant categories: "+node.getNumberOfRecordsFromChildren());
        result.add("LogRecords in this category including descendants: "+node.getTotalNumberOfRecords());
        return result.toArray();
    }
    protected void showPopup(final CategoryNode node,final int x,final int y){
        final JPopupMenu popup=new JPopupMenu();
        popup.setSize(150,400);
        if(node.getParent()==null){
            popup.add(this.createRemoveMenuItem());
            popup.addSeparator();
        }
        popup.add(this.createSelectDescendantsMenuItem(node));
        popup.add(this.createUnselectDescendantsMenuItem(node));
        popup.addSeparator();
        popup.add(this.createExpandMenuItem(node));
        popup.add(this.createCollapseMenuItem(node));
        popup.addSeparator();
        popup.add(this.createPropertiesMenuItem(node));
        popup.show(this._renderer,x,y);
    }
    protected JMenuItem createSelectDescendantsMenuItem(final CategoryNode node){
        final JMenuItem selectDescendants=new JMenuItem("Select All Descendant Categories");
        selectDescendants.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this._categoryModel.setDescendantSelection(node,true);
            }
        });
        return selectDescendants;
    }
    protected JMenuItem createUnselectDescendantsMenuItem(final CategoryNode node){
        final JMenuItem unselectDescendants=new JMenuItem("Deselect All Descendant Categories");
        unselectDescendants.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this._categoryModel.setDescendantSelection(node,false);
            }
        });
        return unselectDescendants;
    }
    protected JMenuItem createExpandMenuItem(final CategoryNode node){
        final JMenuItem result=new JMenuItem("Expand All Descendant Categories");
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this.expandDescendants(node);
            }
        });
        return result;
    }
    protected JMenuItem createCollapseMenuItem(final CategoryNode node){
        final JMenuItem result=new JMenuItem("Collapse All Descendant Categories");
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                CategoryNodeEditor.this.collapseDescendants(node);
            }
        });
        return result;
    }
    protected JMenuItem createRemoveMenuItem(){
        final JMenuItem result=new JMenuItem("Remove All Empty Categories");
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                while(CategoryNodeEditor.this.removeUnusedNodes()>0){
                }
            }
        });
        return result;
    }
    protected void expandDescendants(final CategoryNode node){
        final Enumeration descendants=node.depthFirstEnumeration();
        while(descendants.hasMoreElements()){
            final CategoryNode current=descendants.nextElement();
            this.expand(current);
        }
    }
    protected void collapseDescendants(final CategoryNode node){
        final Enumeration descendants=node.depthFirstEnumeration();
        while(descendants.hasMoreElements()){
            final CategoryNode current=descendants.nextElement();
            this.collapse(current);
        }
    }
    protected int removeUnusedNodes(){
        int count=0;
        final CategoryNode root=this._categoryModel.getRootCategoryNode();
        final Enumeration enum1=root.depthFirstEnumeration();
        while(enum1.hasMoreElements()){
            final CategoryNode node=enum1.nextElement();
            if(node.isLeaf()&&node.getNumberOfContainedRecords()==0&&node.getParent()!=null){
                this._categoryModel.removeNodeFromParent(node);
                ++count;
            }
        }
        return count;
    }
    protected void expand(final CategoryNode node){
        this._tree.expandPath(this.getTreePath(node));
    }
    protected TreePath getTreePath(final CategoryNode node){
        return new TreePath(node.getPath());
    }
    protected void collapse(final CategoryNode node){
        this._tree.collapsePath(this.getTreePath(node));
    }
}
