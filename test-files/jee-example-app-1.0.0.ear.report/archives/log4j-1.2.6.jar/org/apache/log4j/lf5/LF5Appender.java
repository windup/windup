package org.apache.log4j.lf5;

import java.awt.Toolkit;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.lf5.LogRecord;
import org.apache.log4j.lf5.LogLevelFormatException;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.Log4JLogRecord;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.lf5.AppenderFinalizer;
import org.apache.log4j.lf5.viewer.LogBrokerMonitor;
import org.apache.log4j.AppenderSkeleton;

public class LF5Appender extends AppenderSkeleton{
    protected LogBrokerMonitor _logMonitor;
    protected static LogBrokerMonitor _defaultLogMonitor;
    protected static AppenderFinalizer _finalizer;
    public LF5Appender(){
        this(getDefaultInstance());
    }
    public LF5Appender(final LogBrokerMonitor monitor){
        super();
        if(monitor!=null){
            this._logMonitor=monitor;
        }
    }
    public void append(final LoggingEvent event){
        final String category=event.getLoggerName();
        final String logMessage=event.getRenderedMessage();
        final String nestedDiagnosticContext=event.getNDC();
        final String threadDescription=event.getThreadName();
        final String level=event.getLevel().toString();
        final long time=event.timeStamp;
        final LocationInfo locationInfo=event.getLocationInformation();
        final Log4JLogRecord record=new Log4JLogRecord();
        record.setCategory(category);
        record.setMessage(logMessage);
        record.setLocation(locationInfo.fullInfo);
        record.setMillis(time);
        record.setThreadDescription(threadDescription);
        if(nestedDiagnosticContext!=null){
            record.setNDC(nestedDiagnosticContext);
        }
        else{
            record.setNDC("");
        }
        if(event.getThrowableInformation()!=null){
            record.setThrownStackTrace(event.getThrowableInformation());
        }
        try{
            record.setLevel(LogLevel.valueOf(level));
        }
        catch(LogLevelFormatException e){
            record.setLevel(LogLevel.WARN);
        }
        if(this._logMonitor!=null){
            this._logMonitor.addMessage(record);
        }
    }
    public void close(){
    }
    public boolean requiresLayout(){
        return false;
    }
    public void setCallSystemExitOnClose(final boolean callSystemExitOnClose){
        this._logMonitor.setCallSystemExitOnClose(callSystemExitOnClose);
    }
    public boolean equals(final LF5Appender compareTo){
        return this._logMonitor==compareTo.getLogBrokerMonitor();
    }
    public LogBrokerMonitor getLogBrokerMonitor(){
        return this._logMonitor;
    }
    public static void main(final String[] args){
        new LF5Appender();
    }
    public void setMaxNumberOfRecords(final int maxNumberOfRecords){
        LF5Appender._defaultLogMonitor.setMaxNumberOfLogRecords(maxNumberOfRecords);
    }
    protected static synchronized LogBrokerMonitor getDefaultInstance(){
        if(LF5Appender._defaultLogMonitor==null){
            try{
                LF5Appender._defaultLogMonitor=new LogBrokerMonitor(LogLevel.getLog4JLevels());
                LF5Appender._finalizer=new AppenderFinalizer(LF5Appender._defaultLogMonitor);
                LF5Appender._defaultLogMonitor.setFrameSize(getDefaultMonitorWidth(),getDefaultMonitorHeight());
                LF5Appender._defaultLogMonitor.setFontSize(12);
                LF5Appender._defaultLogMonitor.show();
            }
            catch(SecurityException e){
                LF5Appender._defaultLogMonitor=null;
            }
        }
        return LF5Appender._defaultLogMonitor;
    }
    protected static int getScreenWidth(){
        try{
            return Toolkit.getDefaultToolkit().getScreenSize().width;
        }
        catch(Throwable t){
            return 800;
        }
    }
    protected static int getScreenHeight(){
        try{
            return Toolkit.getDefaultToolkit().getScreenSize().height;
        }
        catch(Throwable t){
            return 600;
        }
    }
    protected static int getDefaultMonitorWidth(){
        return 3*getScreenWidth()/4;
    }
    protected static int getDefaultMonitorHeight(){
        return 3*getScreenHeight()/4;
    }
}
