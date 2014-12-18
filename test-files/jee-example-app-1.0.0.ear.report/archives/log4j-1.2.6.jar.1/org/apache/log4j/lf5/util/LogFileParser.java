package org.apache.log4j.lf5.util;

import org.apache.log4j.lf5.Log4JLogRecord;
import org.apache.log4j.lf5.LogLevelFormatException;
import org.apache.log4j.lf5.LogLevel;
import java.util.Date;
import java.text.ParseException;
import java.io.BufferedInputStream;
import org.apache.log4j.lf5.viewer.LogFactor5ErrorDialog;
import org.apache.log4j.lf5.LogRecord;
import javax.swing.SwingUtilities;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import org.apache.log4j.lf5.viewer.LogFactor5LoadingDialog;
import org.apache.log4j.lf5.viewer.LogBrokerMonitor;
import java.text.SimpleDateFormat;

public class LogFileParser implements Runnable{
    public static final String RECORD_DELIMITER="[slf5s.start]";
    public static final String ATTRIBUTE_DELIMITER="[slf5s.";
    public static final String DATE_DELIMITER="[slf5s.DATE]";
    public static final String THREAD_DELIMITER="[slf5s.THREAD]";
    public static final String CATEGORY_DELIMITER="[slf5s.CATEGORY]";
    public static final String LOCATION_DELIMITER="[slf5s.LOCATION]";
    public static final String MESSAGE_DELIMITER="[slf5s.MESSAGE]";
    public static final String PRIORITY_DELIMITER="[slf5s.PRIORITY]";
    public static final String NDC_DELIMITER="[slf5s.NDC]";
    private static SimpleDateFormat _sdf;
    private LogBrokerMonitor _monitor;
    LogFactor5LoadingDialog _loadDialog;
    private InputStream _in;
    public LogFileParser(final File file) throws IOException,FileNotFoundException{
        this(new FileInputStream(file));
    }
    public LogFileParser(final InputStream stream) throws IOException{
        super();
        this._in=null;
        this._in=stream;
    }
    public void parse(final LogBrokerMonitor monitor) throws RuntimeException{
        this._monitor=monitor;
        final Thread t=new Thread(this);
        t.start();
    }
    public void run(){
        int index=0;
        int counter=0;
        boolean isLogFile=false;
        this._loadDialog=new LogFactor5LoadingDialog(this._monitor.getBaseFrame(),"Loading file...");
        try{
            String logRecords;
            for(logRecords=this.loadLogFile(this._in);(counter=logRecords.indexOf("[slf5s.start]",index))!=-1;index=counter+"[slf5s.start]".length()){
                final LogRecord temp=this.createLogRecord(logRecords.substring(index,counter));
                isLogFile=true;
                if(temp!=null){
                    this._monitor.addMessage(temp);
                }
            }
            if(index<logRecords.length()&&isLogFile){
                final LogRecord temp=this.createLogRecord(logRecords.substring(index));
                if(temp!=null){
                    this._monitor.addMessage(temp);
                }
            }
            if(!isLogFile){
                throw new RuntimeException("Invalid log file format");
            }
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    LogFileParser.this.destroyDialog();
                }
            });
        }
        catch(RuntimeException e){
            this.destroyDialog();
            this.displayError("Error - Invalid log file format.\nPlease see documentation on how to load log files.");
        }
        catch(IOException e2){
            this.destroyDialog();
            this.displayError("Error - Unable to load log file!");
        }
        this._in=null;
    }
    protected void displayError(final String message){
        final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this._monitor.getBaseFrame(),message);
    }
    private void destroyDialog(){
        this._loadDialog.hide();
        this._loadDialog.dispose();
    }
    private String loadLogFile(final InputStream stream) throws IOException{
        BufferedInputStream br=new BufferedInputStream(stream);
        int count=0;
        final int size=br.available();
        StringBuffer sb=null;
        if(size>0){
            sb=new StringBuffer(size);
        }
        else{
            sb=new StringBuffer(1024);
        }
        while((count=br.read())!=-1){
            sb.append((char)count);
        }
        br.close();
        br=null;
        return sb.toString();
    }
    private String parseAttribute(final String name,final String record){
        final int index=record.indexOf(name);
        if(index==-1){
            return null;
        }
        return this.getAttribute(index,record);
    }
    private long parseDate(final String record){
        try{
            final String attribute=this.parseAttribute("[slf5s.DATE]",record);
            if(attribute==null){
                return 0L;
            }
            final Date d=LogFileParser._sdf.parse(attribute);
            return d.getTime();
        }
        catch(ParseException e){
            return 0L;
        }
    }
    private LogLevel parsePriority(final String record){
        final String temp=this.parseAttribute("[slf5s.PRIORITY]",record);
        if(temp!=null){
            try{
                return LogLevel.valueOf(temp);
            }
            catch(LogLevelFormatException e){
                return LogLevel.DEBUG;
            }
        }
        return LogLevel.DEBUG;
    }
    private String parseThread(final String record){
        return this.parseAttribute("[slf5s.THREAD]",record);
    }
    private String parseCategory(final String record){
        return this.parseAttribute("[slf5s.CATEGORY]",record);
    }
    private String parseLocation(final String record){
        return this.parseAttribute("[slf5s.LOCATION]",record);
    }
    private String parseMessage(final String record){
        return this.parseAttribute("[slf5s.MESSAGE]",record);
    }
    private String parseNDC(final String record){
        return this.parseAttribute("[slf5s.NDC]",record);
    }
    private String parseThrowable(final String record){
        return this.getAttribute(record.length(),record);
    }
    private LogRecord createLogRecord(final String record){
        if(record==null||record.trim().length()==0){
            return null;
        }
        final LogRecord lr=new Log4JLogRecord();
        lr.setMillis(this.parseDate(record));
        lr.setLevel(this.parsePriority(record));
        lr.setCategory(this.parseCategory(record));
        lr.setLocation(this.parseLocation(record));
        lr.setThreadDescription(this.parseThread(record));
        lr.setNDC(this.parseNDC(record));
        lr.setMessage(this.parseMessage(record));
        lr.setThrownStackTrace(this.parseThrowable(record));
        return lr;
    }
    private String getAttribute(final int index,final String record){
        int start=record.lastIndexOf("[slf5s.",index-1);
        if(start==-1){
            return record.substring(0,index);
        }
        start=record.indexOf("]",start);
        return record.substring(start+1,index).trim();
    }
    static{
        LogFileParser._sdf=new SimpleDateFormat("dd MMM yyyy HH:mm:ss,S");
    }
}
