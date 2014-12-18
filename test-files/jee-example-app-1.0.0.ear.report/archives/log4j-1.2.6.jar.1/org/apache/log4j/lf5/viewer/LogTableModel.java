package org.apache.log4j.lf5.viewer;

import javax.swing.table.DefaultTableModel;

public class LogTableModel extends DefaultTableModel{
    public LogTableModel(final Object[] colNames,final int numRows){
        super(colNames,numRows);
    }
    public boolean isCellEditable(final int row,final int column){
        return false;
    }
}
