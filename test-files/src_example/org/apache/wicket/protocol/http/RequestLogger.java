package org.apache.wicket.protocol.http;

import org.apache.wicket.util.string.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class RequestLogger extends AbstractRequestLogger{
    private static final Logger LOG;
    protected void log(final IRequestLogger.RequestData rd,final IRequestLogger.SessionData sd){
        if(RequestLogger.LOG.isInfoEnabled()){
            RequestLogger.LOG.info(this.createRequestData(rd,sd));
        }
    }
    private String createRequestData(final IRequestLogger.RequestData rd,final IRequestLogger.SessionData sd){
        final AppendingStringBuffer sb=new AppendingStringBuffer(150);
        sb.append("startTime=\"");
        sb.append(AbstractRequestLogger.formatDate(rd.getStartDate()));
        sb.append("\",duration=");
        sb.append((Object)rd.getTimeTaken());
        sb.append(",url=\"");
        sb.append(rd.getRequestedUrl());
        sb.append("\"");
        sb.append(",event={");
        sb.append(this.getRequestHandlerString(rd.getEventTarget()));
        sb.append("},response={");
        sb.append(this.getRequestHandlerString(rd.getResponseTarget()));
        sb.append("},sessionid=\"");
        sb.append(rd.getSessionId());
        sb.append("\"");
        sb.append(",sessionsize=");
        sb.append((Object)rd.getSessionSize());
        if(rd.getSessionInfo()!=null&&!Strings.isEmpty((CharSequence)rd.getSessionInfo().toString())){
            sb.append(",sessioninfo={");
            sb.append(rd.getSessionInfo());
            sb.append("}");
        }
        if(sd!=null){
            sb.append(",sessionstart=\"");
            sb.append(AbstractRequestLogger.formatDate(sd.getStartDate()));
            sb.append("\",requests=");
            sb.append(sd.getNumberOfRequests());
            sb.append(",totaltime=");
            sb.append(sd.getTotalTimeTaken());
        }
        sb.append(",activerequests=");
        sb.append(rd.getActiveRequest());
        final Runtime runtime=Runtime.getRuntime();
        final long max=runtime.maxMemory()/1000000L;
        final long total=runtime.totalMemory()/1000000L;
        final long used=total-runtime.freeMemory()/1000000L;
        sb.append(",maxmem=");
        sb.append(max);
        sb.append("M,total=");
        sb.append(total);
        sb.append("M,used=");
        sb.append(used);
        sb.append("M");
        return sb.toString();
    }
    private String getRequestHandlerString(final IRequestHandler handler){
        final AppendingStringBuffer sb=new AppendingStringBuffer(128);
        if(handler!=null){
            sb.append("handler=");
            sb.append(handler.getClass().isAnonymousClass()?handler.getClass().getName():handler.getClass().getSimpleName());
            if(handler instanceof ILoggableRequestHandler){
                sb.append(",data=");
                sb.append((Object)((ILoggableRequestHandler)handler).getLogData());
            }
        }
        else{
            sb.append("none");
        }
        return sb.toString();
    }
    static{
        LOG=LoggerFactory.getLogger(RequestLogger.class);
    }
}
