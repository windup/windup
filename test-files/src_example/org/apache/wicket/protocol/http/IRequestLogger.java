package org.apache.wicket.protocol.http;

import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.string.*;

public interface IRequestLogger{
    int getTotalCreatedSessions();
    int getPeakSessions();
    List<RequestData> getRequests();
    SessionData[] getLiveSessions();
    int getCurrentActiveRequestCount();
    int getPeakActiveRequestCount();
    long getRequestsPerMinute();
    long getAverageRequestTime();
    void sessionCreated(String p0);
    void sessionDestroyed(String p0);
    void requestTime(long p0);
    void objectRemoved(Object p0);
    void objectUpdated(Object p0);
    void objectCreated(Object p0);
    void logResponseTarget(IRequestHandler p0);
    void logEventTarget(IRequestHandler p0);
    void logRequestedUrl(String p0);
    public static class SessionData implements IClusterable,Comparable<SessionData>{
        private static final long serialVersionUID=1L;
        private final String sessionId;
        private final long startDate;
        private long lastActive;
        private long numberOfRequests;
        private long totalTimeTaken;
        private long sessionSize;
        private Object sessionInfo;
        public SessionData(String sessionId){
            super();
            this.sessionId=sessionId;
            this.startDate=System.currentTimeMillis();
            this.numberOfRequests=1L;
        }
        public Date getLastActive(){
            return new Date(this.lastActive);
        }
        public Date getStartDate(){
            return new Date(this.startDate);
        }
        public long getNumberOfRequests(){
            return this.numberOfRequests;
        }
        public long getSessionSize(){
            return this.sessionSize;
        }
        public long getTotalTimeTaken(){
            return this.totalTimeTaken;
        }
        public Object getSessionInfo(){
            return this.sessionInfo;
        }
        public String getSessionId(){
            return this.sessionId;
        }
        public void addTimeTaken(long time){
            this.lastActive=System.currentTimeMillis();
            ++this.numberOfRequests;
            this.totalTimeTaken+=time;
        }
        public void setSessionInfo(Object sessionInfo){
            this.sessionInfo=sessionInfo;
        }
        public void setSessionSize(long size){
            this.sessionSize=size;
        }
        public int compareTo(SessionData sd){
            int result;
            result=0;
            if(sd.lastActive>this.lastActive){
                result=1;
            }
            else if(sd.lastActive<this.lastActive){
                result=-1;
            }
            return result;
        }
    }
    public static class RequestData implements IClusterable{
        private static final long serialVersionUID=1L;
        private long startDate;
        private long timeTaken;
        private final List<String> entries;
        private String requestedUrl;
        private IRequestHandler eventTarget;
        private IRequestHandler responseTarget;
        private String sessionId;
        private long totalSessionSize;
        private Object sessionInfo;
        private int activeRequest;
        public RequestData(){
            super();
            this.entries=(List<String>)new ArrayList(5);
        }
        public Long getTimeTaken(){
            return this.timeTaken;
        }
        public void setActiveRequest(int activeRequest){
            this.activeRequest=activeRequest;
        }
        public int getActiveRequest(){
            return this.activeRequest;
        }
        public Object getSessionInfo(){
            return this.sessionInfo;
        }
        public void setSessionInfo(Object sessionInfo){
            this.sessionInfo=sessionInfo;
        }
        public void setSessionSize(long sizeInBytes){
            this.totalSessionSize=sizeInBytes;
        }
        public void setSessionId(String id){
            this.sessionId=id;
        }
        public Date getStartDate(){
            return new Date(this.startDate);
        }
        public IRequestHandler getEventTarget(){
            return this.eventTarget;
        }
        public Class<? extends IRequestHandler> getEventTargetClass(){
            return (Class<? extends IRequestHandler>)((this.eventTarget==null)?null:this.eventTarget.getClass());
        }
        public ILogData getEventTargetLog(){
            if(this.eventTarget instanceof ILoggableRequestHandler){
                return ((ILoggableRequestHandler)this.eventTarget).getLogData();
            }
            return (ILogData)new NoLogData();
        }
        public IRequestHandler getResponseTarget(){
            return this.responseTarget;
        }
        public Class<? extends IRequestHandler> getResponseTargetClass(){
            return (Class<? extends IRequestHandler>)((this.responseTarget==null)?null:this.responseTarget.getClass());
        }
        public ILogData getResponseTargetLog(){
            if(this.responseTarget instanceof ILoggableRequestHandler){
                return ((ILoggableRequestHandler)this.responseTarget).getLogData();
            }
            return (ILogData)new NoLogData();
        }
        public String getRequestedUrl(){
            return this.requestedUrl;
        }
        public void setRequestedUrl(String requestedUrl){
            this.requestedUrl=requestedUrl;
        }
        public void setResponseTarget(IRequestHandler target){
            this.responseTarget=target;
        }
        public void setEventTarget(IRequestHandler target){
            this.eventTarget=target;
        }
        public void setTimeTaken(long timeTaken){
            this.timeTaken=timeTaken;
            this.startDate=System.currentTimeMillis()-timeTaken;
        }
        public void addEntry(String string){
            this.entries.add(string);
        }
        public String getAlteredObjects(){
            return Strings.join(", ",(List)this.entries);
        }
        public String getSessionId(){
            return this.sessionId;
        }
        public Long getSessionSize(){
            return this.totalSessionSize;
        }
        public String toString(){
            return "Request[timetaken="+this.getTimeTaken()+",sessioninfo="+this.sessionInfo+",sessionid="+this.sessionId+",sessionsize="+this.totalSessionSize+",request="+this.eventTarget+",response="+this.responseTarget+",alteredobjects="+this.getAlteredObjects()+",activerequest="+this.activeRequest+"]";
        }
    }
    public interface ISessionLogInfo{
        Object getSessionInfo();
    }
}
