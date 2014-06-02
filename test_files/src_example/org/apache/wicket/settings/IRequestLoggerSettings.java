package org.apache.wicket.settings;

public interface IRequestLoggerSettings{
    void setRequestLoggerEnabled(boolean p0);
    boolean isRequestLoggerEnabled();
    void setRecordSessionSize(boolean p0);
    boolean getRecordSessionSize();
    void setRequestsWindowSize(int p0);
    int getRequestsWindowSize();
}
