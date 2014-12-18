package org.apache.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.Filter;

public interface Appender{
    void addFilter(Filter p0);
    Filter getFilter();
    void clearFilters();
    void close();
    void doAppend(LoggingEvent p0);
    String getName();
    void setErrorHandler(ErrorHandler p0);
    ErrorHandler getErrorHandler();
    void setLayout(Layout p0);
    Layout getLayout();
    void setName(String p0);
    boolean requiresLayout();
}
