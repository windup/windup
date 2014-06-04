package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.util.lang.*;

public class ExceptionSettings implements IExceptionSettings{
    private UnexpectedExceptionDisplay unexpectedExceptionDisplay;
    private AjaxErrorStrategy errorHandlingStrategyDuringAjaxRequests;
    private ThreadDumpStrategy threadDumpStrategy;
    public ExceptionSettings(){
        super();
        this.unexpectedExceptionDisplay=ExceptionSettings.SHOW_EXCEPTION_PAGE;
        this.errorHandlingStrategyDuringAjaxRequests=AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE;
        this.threadDumpStrategy=ThreadDumpStrategy.THREAD_HOLDING_LOCK;
    }
    public UnexpectedExceptionDisplay getUnexpectedExceptionDisplay(){
        return this.unexpectedExceptionDisplay;
    }
    public void setUnexpectedExceptionDisplay(final UnexpectedExceptionDisplay unexpectedExceptionDisplay){
        this.unexpectedExceptionDisplay=unexpectedExceptionDisplay;
    }
    public AjaxErrorStrategy getAjaxErrorHandlingStrategy(){
        return this.errorHandlingStrategyDuringAjaxRequests;
    }
    public void setAjaxErrorHandlingStrategy(final AjaxErrorStrategy errorHandlingStrategyDuringAjaxRequests){
        this.errorHandlingStrategyDuringAjaxRequests=errorHandlingStrategyDuringAjaxRequests;
    }
    public void setThreadDumpStrategy(final ThreadDumpStrategy strategy){
        this.threadDumpStrategy=(ThreadDumpStrategy)Args.notNull((Object)strategy,"strategy");
    }
    public ThreadDumpStrategy getThreadDumpStrategy(){
        return this.threadDumpStrategy;
    }
}
