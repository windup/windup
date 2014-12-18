package org.apache.log4j.lf5.viewer;

import javax.swing.event.ListSelectionEvent;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Font;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.TableColumnModel;
import javax.swing.ListSelectionModel;
import java.util.Enumeration;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.apache.log4j.lf5.viewer.LogTableRowRenderer;
import javax.swing.table.TableModel;
import org.apache.log4j.lf5.viewer.FilteredLogTableModel;
import org.apache.log4j.lf5.util.DateFormatManager;
import org.apache.log4j.lf5.viewer.LogTableColumn;
import javax.swing.table.TableColumn;
import javax.swing.JTextArea;
import javax.swing.JTable;

public class LogTable extends JTable{
    protected int _rowHeight;
    protected JTextArea _detailTextArea;
    protected int _numCols;
    protected TableColumn[] _tableColumns;
    protected int[] _colWidths;
    protected LogTableColumn[] _colNames;
    protected int _colDate;
    protected int _colThread;
    protected int _colMessageNum;
    protected int _colLevel;
    protected int _colNDC;
    protected int _colCategory;
    protected int _colMessage;
    protected int _colLocation;
    protected int _colThrown;
    protected DateFormatManager _dateFormatManager;
    public LogTable(final JTextArea detailTextArea){
        super();
        this._rowHeight=30;
        this._numCols=9;
        this._tableColumns=new TableColumn[this._numCols];
        this._colWidths=new int[] { 40,40,40,70,70,360,440,200,60 };
        this._colNames=LogTableColumn.getLogTableColumnArray();
        this._colDate=0;
        this._colThread=1;
        this._colMessageNum=2;
        this._colLevel=3;
        this._colNDC=4;
        this._colCategory=5;
        this._colMessage=6;
        this._colLocation=7;
        this._colThrown=8;
        this._dateFormatManager=null;
        this.init();
        this._detailTextArea=detailTextArea;
        this.setModel(new FilteredLogTableModel());
        final Enumeration columns=this.getColumnModel().getColumns();
        int i=0;
        while(columns.hasMoreElements()){
            final TableColumn col=columns.nextElement();
            col.setCellRenderer(new LogTableRowRenderer());
            col.setPreferredWidth(this._colWidths[i]);
            this._tableColumns[i]=col;
            ++i;
        }
        final ListSelectionModel rowSM=this.getSelectionModel();
        rowSM.addListSelectionListener(new LogTableListSelectionListener(this));
    }
    public DateFormatManager getDateFormatManager(){
        return this._dateFormatManager;
    }
    public void setDateFormatManager(final DateFormatManager dfm){
        this._dateFormatManager=dfm;
    }
    public synchronized void clearLogRecords(){
        this.getFilteredLogTableModel().clear();
    }
    public FilteredLogTableModel getFilteredLogTableModel(){
        return (FilteredLogTableModel)this.getModel();
    }
    public void setDetailedView(){
        final TableColumnModel model=this.getColumnModel();
        for(int f=0;f<this._numCols;++f){
            model.removeColumn(this._tableColumns[f]);
        }
        for(int i=0;i<this._numCols;++i){
            model.addColumn(this._tableColumns[i]);
        }
        this.sizeColumnsToFit(-1);
    }
    public void setView(final List columns){
        final TableColumnModel model=this.getColumnModel();
        for(int f=0;f<this._numCols;++f){
            model.removeColumn(this._tableColumns[f]);
        }
        final Iterator selectedColumns=columns.iterator();
        final Vector columnNameAndNumber=this.getColumnNameAndNumber();
        while(selectedColumns.hasNext()){
            model.addColumn(this._tableColumns[columnNameAndNumber.indexOf(selectedColumns.next())]);
        }
        this.sizeColumnsToFit(-1);
    }
    public void setFont(final Font font){
        super.setFont(font);
        final Graphics g=this.getGraphics();
        if(g!=null){
            final FontMetrics fm=g.getFontMetrics(font);
            final int height=fm.getHeight();
            this.setRowHeight(this._rowHeight=height+height/3);
        }
    }
    protected void init(){
        this.setRowHeight(this._rowHeight);
        this.setSelectionMode(0);
    }
    protected Vector getColumnNameAndNumber(){
        final Vector columnNameAndNumber=new Vector();
        for(int i=0;i<this._colNames.length;++i){
            columnNameAndNumber.add(i,this._colNames[i]);
        }
        return columnNameAndNumber;
    }
    class LogTableListSelectionListener implements ListSelectionListener{
        protected JTable _table;
        public LogTableListSelectionListener(final JTable table){
            super();
            this._table=table;
        }
        public void valueChanged(final ListSelectionEvent e){
            if(e.getValueIsAdjusting()){
                return;
            }
            final ListSelectionModel lsm=(ListSelectionModel)e.getSource();
            if(!lsm.isSelectionEmpty()){
                final StringBuffer buf=new StringBuffer();
                final int selectedRow=lsm.getMinSelectionIndex();
                for(int i=0;i<LogTable.this._numCols-1;++i){
                    String value="";
                    final Object obj=this._table.getModel().getValueAt(selectedRow,i);
                    if(obj!=null){
                        value=obj.toString();
                    }
                    buf.append(LogTable.this._colNames[i]+":");
                    buf.append("\t");
                    if(i==LogTable.this._colThread||i==LogTable.this._colMessage||i==LogTable.this._colLevel){
                        buf.append("\t");
                    }
                    if(i==LogTable.this._colDate||i==LogTable.this._colNDC){
                        buf.append("\t\t");
                    }
                    buf.append(value);
                    buf.append("\n");
                }
                buf.append(LogTable.this._colNames[LogTable.this._numCols-1]+":\n");
                final Object obj2=this._table.getModel().getValueAt(selectedRow,LogTable.this._numCols-1);
                if(obj2!=null){
                    buf.append(obj2.toString());
                }
                LogTable.this._detailTextArea.setText(buf.toString());
            }
        }
    }
}
