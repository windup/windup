package org.apache.log4j.spi;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;

public interface HierarchyEventListener{
    void addAppenderEvent(Category p0,Appender p1);
    void removeAppenderEvent(Category p0,Appender p1);
}
