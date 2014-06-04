package org.apache.wicket.markup.html.tree;

import java.io.*;
import java.util.*;

public interface ITreeState extends Serializable{
    void addTreeStateListener(ITreeStateListener p0);
    void collapseAll();
    void collapseNode(Object p0);
    void expandAll();
    void expandNode(Object p0);
    Collection<Object> getSelectedNodes();
    boolean isAllowSelectMultiple();
    boolean isNodeExpanded(Object p0);
    boolean isNodeSelected(Object p0);
    void removeTreeStateListener(ITreeStateListener p0);
    void selectNode(Object p0,boolean p1);
    void setAllowSelectMultiple(boolean p0);
}
