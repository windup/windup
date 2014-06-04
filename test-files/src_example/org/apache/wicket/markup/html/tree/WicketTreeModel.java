package org.apache.wicket.markup.html.tree;

import org.apache.wicket.model.util.*;
import javax.swing.tree.*;

public class WicketTreeModel extends GenericBaseModel<TreeModel>{
    private static final long serialVersionUID=1L;
    public WicketTreeModel(){
        super();
    }
    public WicketTreeModel(final TreeModel treeModel){
        super();
        this.setObject(treeModel);
    }
    protected TreeModel createSerializableVersionOf(final TreeModel object){
        return object;
    }
}
