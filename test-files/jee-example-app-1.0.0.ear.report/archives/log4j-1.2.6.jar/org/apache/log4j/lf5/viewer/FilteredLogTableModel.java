package org.apache.log4j.lf5.viewer;

import java.util.Date;
import java.util.Iterator;
import org.apache.log4j.lf5.LogRecord;
import java.util.ArrayList;
import org.apache.log4j.lf5.PassingLogRecordFilter;
import java.util.List;
import org.apache.log4j.lf5.LogRecordFilter;
import javax.swing.table.AbstractTableModel;

public class FilteredLogTableModel extends AbstractTableModel{
    protected LogRecordFilter _filter;
    protected List _allRecords;
    protected List _filteredRecords;
    protected int _maxNumberOfLogRecords;
    protected String[] _colNames;
    public FilteredLogTableModel(){
        super();
        this._filter=new PassingLogRecordFilter();
        this._allRecords=new ArrayList();
        this._maxNumberOfLogRecords=5000;
        this._colNames=new String[] { "Date","Thread","Message #","Level","NDC","Category","Message","Location","Thrown" };
    }
    public void setLogRecordFilter(final LogRecordFilter filter){
        this._filter=filter;
    }
    public LogRecordFilter getLogRecordFilter(){
        return this._filter;
    }
    public String getColumnName(final int i){
        return this._colNames[i];
    }
    public int getColumnCount(){
        return this._colNames.length;
    }
    public int getRowCount(){
        return this.getFilteredRecords().size();
    }
    public int getTotalRowCount(){
        return this._allRecords.size();
    }
    public Object getValueAt(final int row,final int col){
        final LogRecord record=this.getFilteredRecord(row);
        return this.getColumn(col,record);
    }
    public void setMaxNumberOfLogRecords(final int maxNumRecords){
        if(maxNumRecords>0){
            this._maxNumberOfLogRecords=maxNumRecords;
        }
    }
    public synchronized boolean addLogRecord(final LogRecord record){
        this._allRecords.add(record);
        if(!this._filter.passes(record)){
            return false;
        }
        this.getFilteredRecords().add(record);
        this.fireTableRowsInserted(this.getRowCount(),this.getRowCount());
        this.trimRecords();
        return true;
    }
    public synchronized void refresh(){
        this._filteredRecords=this.createFilteredRecordsList();
        this.fireTableDataChanged();
    }
    public synchronized void fastRefresh(){
        this._filteredRecords.remove(0);
        this.fireTableRowsDeleted(0,0);
    }
    public synchronized void clear(){
        this._allRecords.clear();
        this._filteredRecords.clear();
        this.fireTableDataChanged();
    }
    protected List getFilteredRecords(){
        if(this._filteredRecords==null){
            this.refresh();
        }
        return this._filteredRecords;
    }
    protected List createFilteredRecordsList(){
        final List result=new ArrayList();
        for(final LogRecord current : this._allRecords){
            if(this._filter.passes(current)){
                result.add(current);
            }
        }
        return result;
    }
    protected LogRecord getFilteredRecord(final int row){
        final List records=this.getFilteredRecords();
        final int size=records.size();
        if(row<size){
            return records.get(row);
        }
        return records.get(size-1);
    }
    protected Object getColumn(final int col,final LogRecord lr){
        if(lr==null){
            return "NULL Column";
        }
        final String date=new Date(lr.getMillis()).toString();
        switch(col){
            case 0:{
                return date+" ("+lr.getMillis()+")";
            }
            case 1:{
                return lr.getThreadDescription();
            }
            case 2:{
                return new Long(lr.getSequenceNumber());
            }
            case 3:{
                return lr.getLevel();
            }
            case 4:{
                return lr.getNDC();
            }
            case 5:{
                return lr.getCategory();
            }
            case 6:{
                return lr.getMessage();
            }
            case 7:{
                return lr.getLocation();
            }
            case 8:{
                return lr.getThrownStackTrace();
            }
            default:{
                final String message="The column number "+col+"must be between 0 and 8";
                throw new IllegalArgumentException(message);
            }
        }
    }
    protected void trimRecords(){
        if(this.needsTrimming()){
            this.trimOldestRecords();
        }
    }
    protected boolean needsTrimming(){
        return this._allRecords.size()>this._maxNumberOfLogRecords;
    }
    protected void trimOldestRecords(){
        synchronized(this._allRecords){
            final int trim=this.numberOfRecordsToTrim();
            if(trim>1){
                final List oldRecords=this._allRecords.subList(0,trim);
                oldRecords.clear();
                this.refresh();
            }
            else{
                this._allRecords.remove(0);
                this.fastRefresh();
            }
        }
    }
    private int numberOfRecordsToTrim(){
        return this._allRecords.size()-this._maxNumberOfLogRecords;
    }
}
