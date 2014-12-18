package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

public abstract class Layout implements OptionHandler{
    public static final String LINE_SEP;
    public static final int LINE_SEP_LEN;
    public abstract String format(final LoggingEvent p0);
    public String getContentType(){
        return "text/plain";
    }
    public String getHeader(){
        return null;
    }
    public String getFooter(){
        return null;
    }
    public abstract boolean ignoresThrowable();
    public abstract void activateOptions();
    static{
        LINE_SEP=System.getProperty("line.separator");
        LINE_SEP_LEN=Layout.LINE_SEP.length();
    }
}
