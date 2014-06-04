package org.apache.wicket.markup.html.tree;

import java.io.*;

public interface ITreeStateListener extends Serializable{
    void allNodesCollapsed();
    void allNodesExpanded();
    void nodeCollapsed(Object p0);
    void nodeExpanded(Object p0);
    void nodeSelected(Object p0);
    void nodeUnselected(Object p0);
}
