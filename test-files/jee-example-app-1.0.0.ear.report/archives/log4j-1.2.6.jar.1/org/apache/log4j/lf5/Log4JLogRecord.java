package org.apache.log4j.lf5;

import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogRecord;

public class Log4JLogRecord extends LogRecord{
    public boolean isSevereLevel(){
        boolean isSevere=false;
        if(LogLevel.ERROR.equals(this.getLevel())||LogLevel.FATAL.equals(this.getLevel())){
            isSevere=true;
        }
        return isSevere;
    }
    public void setThrownStackTrace(final ThrowableInformation throwableInfo){
        final String[] stackTraceArray=throwableInfo.getThrowableStrRep();
        final StringBuffer stackTrace=new StringBuffer();
        for(int i=0;i<stackTraceArray.length;++i){
            final String nextLine=stackTraceArray[i]+"\n";
            stackTrace.append(nextLine);
        }
        super._thrownStackTrace=stackTrace.toString();
    }
}
