package org.apache.log4j.lf5.viewer;

import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;
import org.apache.log4j.lf5.viewer.FilteredLogTableModel;
import java.awt.Component;
import javax.swing.JTable;
import java.awt.Color;
import javax.swing.table.DefaultTableCellRenderer;

public class LogTableRowRenderer extends DefaultTableCellRenderer{
    protected boolean _highlightFatal;
    protected Color _color;
    public LogTableRowRenderer(){
        super();
        this._highlightFatal=true;
        this._color=new Color(230,230,230);
    }
    public Component getTableCellRendererComponent(final JTable table,final Object value,final boolean isSelected,final boolean hasFocus,final int row,final int col){
        if(row%2==0){
            this.setBackground(this._color);
        }
        else{
            this.setBackground(Color.white);
        }
        final FilteredLogTableModel model=(FilteredLogTableModel)table.getModel();
        final LogRecord record=model.getFilteredRecord(row);
        this.setForeground(this.getLogLevelColor(record.getLevel()));
        return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
    }
    protected Color getLogLevelColor(final LogLevel level){
        return LogLevel.getLogLevelColorMap().get(level);
    }
}
