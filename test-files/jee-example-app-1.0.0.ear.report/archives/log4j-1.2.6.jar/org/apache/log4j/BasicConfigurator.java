package org.apache.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Logger;

public class BasicConfigurator{
    public static void configure(){
        final Logger root=Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n")));
    }
    public static void configure(final Appender appender){
        final Logger root=Logger.getRootLogger();
        root.addAppender(appender);
    }
    public static void resetConfiguration(){
        LogManager.resetConfiguration();
    }
}
