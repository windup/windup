package org.apache.log4j.lf5.viewer;

import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.lf5.viewer.LogTableColumnFormatException;
import java.util.Map;
import java.io.Serializable;

public class LogTableColumn implements Serializable{
    public static final LogTableColumn DATE;
    public static final LogTableColumn THREAD;
    public static final LogTableColumn MESSAGE_NUM;
    public static final LogTableColumn LEVEL;
    public static final LogTableColumn NDC;
    public static final LogTableColumn CATEGORY;
    public static final LogTableColumn MESSAGE;
    public static final LogTableColumn LOCATION;
    public static final LogTableColumn THROWN;
    protected String _label;
    private static LogTableColumn[] _log4JColumns;
    private static Map _logTableColumnMap;
    public LogTableColumn(final String label){
        super();
        this._label=label;
    }
    public String getLabel(){
        return this._label;
    }
    public static LogTableColumn valueOf(String column) throws LogTableColumnFormatException{
        LogTableColumn tableColumn=null;
        if(column!=null){
            column=column.trim();
            tableColumn=LogTableColumn._logTableColumnMap.get(column);
        }
        if(tableColumn==null){
            final StringBuffer buf=new StringBuffer();
            buf.append("Error while trying to parse ("+column+") into");
            buf.append(" a LogTableColumn.");
            throw new LogTableColumnFormatException(buf.toString());
        }
        return tableColumn;
    }
    public boolean equals(final Object o){
        boolean equals=false;
        if(o instanceof LogTableColumn&&this.getLabel()==((LogTableColumn)o).getLabel()){
            equals=true;
        }
        return equals;
    }
    public int hashCode(){
        return this._label.hashCode();
    }
    public String toString(){
        return this._label;
    }
    public static List getLogTableColumns(){
        return Arrays.asList(LogTableColumn._log4JColumns);
    }
    public static LogTableColumn[] getLogTableColumnArray(){
        return LogTableColumn._log4JColumns;
    }
    static{
        DATE=new LogTableColumn("Date");
        THREAD=new LogTableColumn("Thread");
        MESSAGE_NUM=new LogTableColumn("Message #");
        LEVEL=new LogTableColumn("Level");
        NDC=new LogTableColumn("NDC");
        CATEGORY=new LogTableColumn("Category");
        MESSAGE=new LogTableColumn("Message");
        LOCATION=new LogTableColumn("Location");
        THROWN=new LogTableColumn("Thrown");
        LogTableColumn._log4JColumns=new LogTableColumn[] { LogTableColumn.DATE,LogTableColumn.THREAD,LogTableColumn.MESSAGE_NUM,LogTableColumn.LEVEL,LogTableColumn.NDC,LogTableColumn.CATEGORY,LogTableColumn.MESSAGE,LogTableColumn.LOCATION,LogTableColumn.THROWN };
        LogTableColumn._logTableColumnMap=new HashMap();
        for(int i=0;i<LogTableColumn._log4JColumns.length;++i){
            LogTableColumn._logTableColumnMap.put(LogTableColumn._log4JColumns[i].getLabel(),LogTableColumn._log4JColumns[i]);
        }
    }
}
