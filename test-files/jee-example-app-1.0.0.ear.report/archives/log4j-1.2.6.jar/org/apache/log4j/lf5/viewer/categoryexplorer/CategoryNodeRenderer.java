package org.apache.log4j.lf5.viewer.categoryexplorer;

import java.awt.Dimension;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNode;
import javax.swing.JTree;
import java.net.URL;
import javax.swing.Icon;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.awt.Color;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CategoryNodeRenderer extends DefaultTreeCellRenderer{
    public static final Color FATAL_CHILDREN;
    protected JCheckBox _checkBox;
    protected JPanel _panel;
    protected static ImageIcon _sat;
    public CategoryNodeRenderer(){
        super();
        this._checkBox=new JCheckBox();
        (this._panel=new JPanel()).setBackground(UIManager.getColor("Tree.textBackground"));
        if(CategoryNodeRenderer._sat==null){
            final String resource="/org/apache/log4j/lf5/viewer/images/channelexplorer_satellite.gif";
            final URL satURL=this.getClass().getResource(resource);
            CategoryNodeRenderer._sat=new ImageIcon(satURL);
        }
        this.setOpaque(false);
        this._checkBox.setOpaque(false);
        this._panel.setOpaque(false);
        this._panel.setLayout(new FlowLayout(0,0,0));
        this._panel.add(this._checkBox);
        this._panel.add(this);
        this.setOpenIcon(CategoryNodeRenderer._sat);
        this.setClosedIcon(CategoryNodeRenderer._sat);
        this.setLeafIcon(CategoryNodeRenderer._sat);
    }
    public Component getTreeCellRendererComponent(final JTree tree,final Object value,final boolean selected,final boolean expanded,final boolean leaf,final int row,final boolean hasFocus){
        final CategoryNode node=(CategoryNode)value;
        super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
        if(row==0){
            this._checkBox.setVisible(false);
        }
        else{
            this._checkBox.setVisible(true);
            this._checkBox.setSelected(node.isSelected());
        }
        final String toolTip=this.buildToolTip(node);
        this._panel.setToolTipText(toolTip);
        if(node.hasFatalChildren()){
            this.setForeground(CategoryNodeRenderer.FATAL_CHILDREN);
        }
        if(node.hasFatalRecords()){
            this.setForeground(Color.red);
        }
        return this._panel;
    }
    public Dimension getCheckBoxOffset(){
        return new Dimension(0,0);
    }
    protected String buildToolTip(final CategoryNode node){
        final StringBuffer result=new StringBuffer();
        result.append(node.getTitle()).append(" contains a total of ");
        result.append(node.getTotalNumberOfRecords());
        result.append(" LogRecords.");
        result.append(" Right-click for more info.");
        return result.toString();
    }
    static{
        FATAL_CHILDREN=new Color(189,113,0);
        CategoryNodeRenderer._sat=null;
    }
}
