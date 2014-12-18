package org.apache.log4j.lf5.viewer.categoryexplorer;

import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNode;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.apache.log4j.lf5.LogRecord;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryExplorerModel;
import org.apache.log4j.lf5.LogRecordFilter;

public class CategoryExplorerLogRecordFilter implements LogRecordFilter{
    protected CategoryExplorerModel _model;
    public CategoryExplorerLogRecordFilter(final CategoryExplorerModel model){
        super();
        this._model=model;
    }
    public boolean passes(final LogRecord record){
        final CategoryPath path=new CategoryPath(record.getCategory());
        return this._model.isCategoryPathActive(path);
    }
    public void reset(){
        this.resetAllNodes();
    }
    protected void resetAllNodes(){
        final Enumeration nodes=this._model.getRootCategoryNode().depthFirstEnumeration();
        while(nodes.hasMoreElements()){
            final CategoryNode current=nodes.nextElement();
            current.resetNumberOfContainedRecords();
            this._model.nodeChanged(current);
        }
    }
}
