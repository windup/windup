package org.apache.wicket.settings;

import org.apache.wicket.util.lang.*;

public interface IExceptionSettings{
    public static final UnexpectedExceptionDisplay SHOW_EXCEPTION_PAGE=new UnexpectedExceptionDisplay("SHOW_EXCEPTION_PAGE");
    public static final UnexpectedExceptionDisplay SHOW_INTERNAL_ERROR_PAGE=new UnexpectedExceptionDisplay("SHOW_INTERNAL_ERROR_PAGE");
    public static final UnexpectedExceptionDisplay SHOW_NO_EXCEPTION_PAGE=new UnexpectedExceptionDisplay("SHOW_NO_EXCEPTION_PAGE");
    UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();
    void setUnexpectedExceptionDisplay(UnexpectedExceptionDisplay p0);
    void setAjaxErrorHandlingStrategy(AjaxErrorStrategy p0);
    AjaxErrorStrategy getAjaxErrorHandlingStrategy();
    void setThreadDumpStrategy(ThreadDumpStrategy p0);
    ThreadDumpStrategy getThreadDumpStrategy();
    public static final class UnexpectedExceptionDisplay extends EnumeratedType{
        private static final long serialVersionUID=1L;
        UnexpectedExceptionDisplay(String name){
            super(name);
        }
    }
    public enum AjaxErrorStrategy{
        REDIRECT_TO_ERROR_PAGE,INVOKE_FAILURE_HANDLER;
    }
    public enum ThreadDumpStrategy{
        NO_THREADS,THREAD_HOLDING_LOCK,ALL_THREADS;
    }
}
