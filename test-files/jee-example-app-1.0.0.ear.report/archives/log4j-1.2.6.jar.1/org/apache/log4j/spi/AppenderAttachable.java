package org.apache.log4j.spi;

import java.util.Enumeration;
import org.apache.log4j.Appender;

public interface AppenderAttachable{
    void addAppender(Appender p0);
    Enumeration getAllAppenders();
    Appender getAppender(String p0);
    boolean isAttached(Appender p0);
    void removeAllAppenders();
    void removeAppender(Appender p0);
    void removeAppender(String p0);
}
