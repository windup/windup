package org.apache.log4j.spi;

import org.apache.log4j.Appender;
import java.util.Enumeration;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.HierarchyEventListener;

public interface LoggerRepository{
    void addHierarchyEventListener(HierarchyEventListener p0);
    boolean isDisabled(int p0);
    void setThreshold(Level p0);
    void setThreshold(String p0);
    void emitNoAppenderWarning(Category p0);
    Level getThreshold();
    Logger getLogger(String p0);
    Logger getLogger(String p0,LoggerFactory p1);
    Logger getRootLogger();
    Logger exists(String p0);
    void shutdown();
    Enumeration getCurrentLoggers();
    Enumeration getCurrentCategories();
    void fireAddAppenderEvent(Category p0,Appender p1);
    void resetConfiguration();
}
