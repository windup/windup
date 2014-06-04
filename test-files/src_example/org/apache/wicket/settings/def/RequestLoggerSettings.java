package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;

public class RequestLoggerSettings implements IRequestLoggerSettings{
    private boolean recordSessionSize;
    private int requestsWindowSize;
    private boolean requestLoggerEnabled;
    public RequestLoggerSettings(){
        super();
        this.recordSessionSize=true;
        this.requestsWindowSize=0;
    }
    public boolean getRecordSessionSize(){
        return this.recordSessionSize;
    }
    public int getRequestsWindowSize(){
        return this.requestsWindowSize;
    }
    public boolean isRequestLoggerEnabled(){
        return this.requestLoggerEnabled;
    }
    public void setRecordSessionSize(final boolean record){
        this.recordSessionSize=record;
    }
    public void setRequestLoggerEnabled(final boolean enable){
        this.requestLoggerEnabled=enable;
    }
    public void setRequestsWindowSize(final int size){
        this.requestsWindowSize=size;
    }
}
