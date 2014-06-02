package org.apache.wicket.protocol.http;

import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.time.*;
import java.util.*;
import org.slf4j.*;

public abstract class AbstractRequestLogger implements IStagedRequestLogger{
    private static final Logger LOG;
    private static MetaDataKey<IRequestLogger.RequestData> REQUEST_DATA;
    private static MetaDataKey<IRequestLogger.SessionData> SESSION_DATA;
    private final AtomicInteger totalCreatedSessions;
    private final AtomicInteger peakSessions;
    private final Map<String,IRequestLogger.SessionData> liveSessions;
    private final AtomicInteger activeRequests;
    private final AtomicInteger peakActiveRequests;
    private IRequestLogger.RequestData[] requestWindow;
    private int indexInWindow;
    private long totalRequestTime;
    private Date startTimeOfOldestRequest;
    public AbstractRequestLogger(){
        super();
        this.totalCreatedSessions=new AtomicInteger();
        this.peakSessions=new AtomicInteger();
        this.activeRequests=new AtomicInteger();
        this.peakActiveRequests=new AtomicInteger();
        this.indexInWindow=0;
        this.totalRequestTime=0L;
        final int requestsWindowSize=this.getRequestsWindowSize();
        this.requestWindow=new IRequestLogger.RequestData[requestsWindowSize];
        this.liveSessions=(Map<String,IRequestLogger.SessionData>)new ConcurrentHashMap();
    }
    public int getCurrentActiveRequestCount(){
        return this.activeRequests.get();
    }
    public int getPeakActiveRequestCount(){
        return this.peakActiveRequests.get();
    }
    public IRequestLogger.SessionData[] getLiveSessions(){
        final IRequestLogger.SessionData[] sessions=(IRequestLogger.SessionData[])this.liveSessions.values().toArray(new IRequestLogger.SessionData[this.liveSessions.values().size()]);
        Arrays.sort(sessions);
        return sessions;
    }
    public int getPeakSessions(){
        return this.peakSessions.get();
    }
    public List<IRequestLogger.RequestData> getRequests(){
        synchronized(this.requestWindow){
            final IRequestLogger.RequestData[] result=new IRequestLogger.RequestData[this.hasBufferRolledOver()?this.requestWindow.length:this.indexInWindow];
            this.copyRequestsInOrder(result);
            return (List<IRequestLogger.RequestData>)Arrays.asList(result);
        }
    }
    private void copyRequestsInOrder(final IRequestLogger.RequestData[] copy){
        if(this.hasBufferRolledOver()){
            final int oldestPos=this.indexInWindow+1;
            if(oldestPos<this.requestWindow.length){
                System.arraycopy(this.requestWindow,oldestPos,copy,0,this.requestWindow.length-oldestPos);
            }
            System.arraycopy(this.requestWindow,0,copy,this.requestWindow.length-oldestPos,this.indexInWindow);
        }
        else{
            System.arraycopy(this.requestWindow,0,copy,0,this.indexInWindow);
        }
    }
    private boolean hasBufferRolledOver(){
        return this.requestWindow[this.requestWindow.length-1]!=null;
    }
    public int getTotalCreatedSessions(){
        return this.totalCreatedSessions.get();
    }
    public void objectCreated(final Object value){
    }
    public void objectRemoved(final Object value){
    }
    public void objectUpdated(final Object value){
    }
    public void requestTime(final long timeTaken){
        final IRequestLogger.RequestData requestdata=RequestCycle.get().getMetaData(AbstractRequestLogger.REQUEST_DATA);
        if(requestdata!=null){
            if(this.activeRequests.get()>0){
                requestdata.setActiveRequest(this.activeRequests.decrementAndGet());
            }
            final Session session=Session.get();
            final String sessionId=session.getId();
            requestdata.setSessionId(sessionId);
            final Object sessionInfo=this.getSessionInfo(session);
            requestdata.setSessionInfo(sessionInfo);
            long sizeInBytes=-1L;
            if(Application.exists()&&Application.get().getRequestLoggerSettings().getRecordSessionSize()){
                try{
                    sizeInBytes=session.getSizeInBytes();
                }
                catch(Exception e){
                    AbstractRequestLogger.LOG.error("Exception while determining the size of the session in the request logger: "+e.getMessage(),e);
                }
            }
            requestdata.setSessionSize(sizeInBytes);
            requestdata.setTimeTaken(timeTaken);
            this.addRequest(requestdata);
            IRequestLogger.SessionData sessiondata=null;
            if(sessionId!=null){
                sessiondata=(IRequestLogger.SessionData)this.liveSessions.get(sessionId);
                if(sessiondata==null){
                    sessiondata=RequestCycle.get().getMetaData(AbstractRequestLogger.SESSION_DATA);
                }
                if(sessiondata==null){
                    this.sessionCreated(sessionId);
                    sessiondata=(IRequestLogger.SessionData)this.liveSessions.get(sessionId);
                }
                if(sessiondata!=null){
                    sessiondata.setSessionInfo(sessionInfo);
                    sessiondata.setSessionSize(sizeInBytes);
                    sessiondata.addTimeTaken(timeTaken);
                    RequestCycle.get().setMetaData(AbstractRequestLogger.SESSION_DATA,sessiondata);
                }
            }
        }
    }
    public void sessionCreated(final String sessionId){
        this.liveSessions.put(sessionId,new IRequestLogger.SessionData(sessionId));
        if(this.liveSessions.size()>this.peakSessions.get()){
            this.peakSessions.set(this.liveSessions.size());
        }
        this.totalCreatedSessions.incrementAndGet();
    }
    public void sessionDestroyed(final String sessionId){
        final RequestCycle requestCycle=RequestCycle.get();
        final IRequestLogger.SessionData sessionData=(IRequestLogger.SessionData)this.liveSessions.remove(sessionId);
        if(requestCycle!=null){
            requestCycle.setMetaData(AbstractRequestLogger.SESSION_DATA,sessionData);
        }
    }
    protected IRequestLogger.RequestData getCurrentRequest(){
        final RequestCycle requestCycle=RequestCycle.get();
        IRequestLogger.RequestData rd=requestCycle.getMetaData(AbstractRequestLogger.REQUEST_DATA);
        if(rd==null){
            rd=new IRequestLogger.RequestData();
            requestCycle.setMetaData(AbstractRequestLogger.REQUEST_DATA,rd);
            final int activeCount=this.activeRequests.incrementAndGet();
            if(activeCount>this.peakActiveRequests.get()){
                this.peakActiveRequests.set(activeCount);
            }
        }
        return rd;
    }
    public void performLogging(){
        final IRequestLogger.RequestData requestdata=RequestCycle.get().getMetaData(AbstractRequestLogger.REQUEST_DATA);
        final IRequestLogger.SessionData sessiondata=RequestCycle.get().getMetaData(AbstractRequestLogger.SESSION_DATA);
        if(requestdata!=null){
            this.log(requestdata,sessiondata);
        }
    }
    protected abstract void log(final IRequestLogger.RequestData p0,final IRequestLogger.SessionData p1);
    private Object getSessionInfo(final Session session){
        if(session instanceof IRequestLogger.ISessionLogInfo){
            return ((IRequestLogger.ISessionLogInfo)session).getSessionInfo();
        }
        return "";
    }
    protected void addRequest(final IRequestLogger.RequestData rd){
        this.resizeBuffer();
        synchronized(this.requestWindow){
            if(this.requestWindow.length==0){
                return;
            }
            final IRequestLogger.RequestData old=this.requestWindow[this.indexInWindow];
            this.requestWindow[this.indexInWindow]=rd;
            this.indexInWindow=(this.indexInWindow+1)%this.requestWindow.length;
            if(old!=null){
                this.startTimeOfOldestRequest=this.requestWindow[this.indexInWindow].getStartDate();
                this.totalRequestTime-=old.getTimeTaken();
            }
            else if(this.startTimeOfOldestRequest==null){
                this.startTimeOfOldestRequest=rd.getStartDate();
            }
            this.totalRequestTime+=rd.getTimeTaken();
        }
    }
    private int getWindowSize(){
        synchronized(this.requestWindow){
            if(this.requestWindow[this.requestWindow.length-1]==null){
                return this.indexInWindow;
            }
            return this.requestWindow.length;
        }
    }
    public long getAverageRequestTime(){
        synchronized(this.requestWindow){
            final int windowSize=this.getWindowSize();
            if(windowSize==0){
                return 0L;
            }
            return this.totalRequestTime/windowSize;
        }
    }
    public long getRequestsPerMinute(){
        synchronized(this.requestWindow){
            final int windowSize=this.getWindowSize();
            if(windowSize==0){
                return 0L;
            }
            final long start=this.startTimeOfOldestRequest.getTime();
            final long end=System.currentTimeMillis();
            final double diff=end-start;
            return Math.round(windowSize/(diff/60000.0));
        }
    }
    public void logEventTarget(final IRequestHandler requestHandler){
        final IRequestLogger.RequestData requestData=this.getCurrentRequest();
        if(requestData!=null){
            requestData.setEventTarget(requestHandler);
        }
    }
    public void logRequestedUrl(final String url){
        this.getCurrentRequest().setRequestedUrl(url);
    }
    public void logResponseTarget(final IRequestHandler requestHandler){
        final IRequestLogger.RequestData requestData=this.getCurrentRequest();
        if(requestData!=null){
            requestData.setResponseTarget(requestHandler);
        }
    }
    private void resizeBuffer(){
        final int newCapacity=this.getRequestsWindowSize();
        if(newCapacity==this.requestWindow.length){
            return;
        }
        final IRequestLogger.RequestData[] newRequestWindow=new IRequestLogger.RequestData[newCapacity];
        synchronized(this.requestWindow){
            final int oldCapacity=this.requestWindow.length;
            final int oldNumberOfElements=this.hasBufferRolledOver()?oldCapacity:this.indexInWindow;
            if(newCapacity>oldCapacity){
                this.copyRequestsInOrder(newRequestWindow);
                this.indexInWindow=oldNumberOfElements;
                this.requestWindow=newRequestWindow;
            }
            else if(newCapacity<oldCapacity){
                final IRequestLogger.RequestData[] sortedRequestWindow=new IRequestLogger.RequestData[oldCapacity];
                this.copyRequestsInOrder(sortedRequestWindow);
                final int numberOfElementsToCopy=Math.min(newCapacity,oldNumberOfElements);
                final int numberOfElementsToSkip=Math.max(0,oldNumberOfElements-numberOfElementsToCopy);
                System.arraycopy(sortedRequestWindow,numberOfElementsToSkip,newRequestWindow,0,numberOfElementsToCopy);
                this.indexInWindow=((numberOfElementsToCopy>=newCapacity)?0:numberOfElementsToCopy);
                this.requestWindow=newRequestWindow;
            }
        }
    }
    protected static String formatDate(final Date date){
        Args.notNull((Object)date,"date");
        final Calendar cal=Calendar.getInstance(Time.GMT);
        final StringBuilder buf=new StringBuilder(32);
        cal.setTimeInMillis(date.getTime());
        final int year=cal.get(1);
        final int month=cal.get(2)+1;
        final int day=cal.get(5);
        final int hours=cal.get(11);
        final int minutes=cal.get(12);
        final int seconds=cal.get(13);
        final int millis=cal.get(14);
        buf.append(year);
        buf.append('-');
        buf.append(String.format("%02d",new Object[] { month }));
        buf.append('-');
        buf.append(String.format("%02d",new Object[] { day }));
        buf.append(' ');
        buf.append(String.format("%02d",new Object[] { hours }));
        buf.append(':');
        buf.append(String.format("%02d",new Object[] { minutes }));
        buf.append(':');
        buf.append(String.format("%02d",new Object[] { seconds }));
        buf.append(',');
        buf.append(String.format("%03d",new Object[] { millis }));
        return buf.toString();
    }
    private int getRequestsWindowSize(){
        int requestsWindowSize=0;
        if(Application.exists()){
            requestsWindowSize=Application.get().getRequestLoggerSettings().getRequestsWindowSize();
        }
        return requestsWindowSize;
    }
    static{
        LOG=LoggerFactory.getLogger(AbstractRequestLogger.class);
        AbstractRequestLogger.REQUEST_DATA=new MetaDataKey<IRequestLogger.RequestData>(){
            private static final long serialVersionUID=1L;
        };
        AbstractRequestLogger.SESSION_DATA=new MetaDataKey<IRequestLogger.SessionData>(){
            private static final long serialVersionUID=1L;
        };
    }
}
