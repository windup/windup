package org.apache.log4j.lf5;

import org.apache.log4j.lf5.LogRecord;
import org.apache.log4j.lf5.LogRecordFilter;

public class PassingLogRecordFilter implements LogRecordFilter{
    public boolean passes(final LogRecord record){
        return true;
    }
    public void reset(){
    }
}
