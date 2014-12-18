package org.apache.log4j.lf5;

import org.apache.log4j.lf5.LogRecord;

public interface LogRecordFilter{
    boolean passes(LogRecord p0);
}
