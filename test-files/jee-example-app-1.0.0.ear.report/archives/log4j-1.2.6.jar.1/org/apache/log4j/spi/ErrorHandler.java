package org.apache.log4j.spi;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.OptionHandler;

public interface ErrorHandler extends OptionHandler{
    void setLogger(Logger p0);
    void error(String p0,Exception p1,int p2);
    void error(String p0);
    void error(String p0,Exception p1,int p2,LoggingEvent p3);
    void setAppender(Appender p0);
    void setBackupAppender(Appender p0);
}
