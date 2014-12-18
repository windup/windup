package org.apache.log4j.xml;

import org.apache.log4j.helpers.LogLog;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

public class SAXErrorHandler implements ErrorHandler{
    public void error(final SAXParseException ex){
        LogLog.error("Parsing error on line "+ex.getLineNumber()+" and column "+ex.getColumnNumber());
        LogLog.error(ex.getMessage(),ex.getException());
    }
    public void fatalError(final SAXParseException ex){
        this.error(ex);
    }
    public void warning(final SAXParseException ex){
        LogLog.warn("Parsing error on line "+ex.getLineNumber()+" and column "+ex.getColumnNumber());
        LogLog.warn(ex.getMessage(),ex.getException());
    }
}
