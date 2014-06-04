package org.apache.wicket;

import org.apache.wicket.request.flow.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.http.handler.*;
import org.apache.wicket.request.*;
import java.io.*;
import org.apache.wicket.util.string.*;
import java.util.*;

public class RestartResponseAtInterceptPageException extends ResetResponseException{
    private static final long serialVersionUID=1L;
    static IRequestMapper MAPPER;
    public RestartResponseAtInterceptPageException(final Page interceptPage){
        super((IRequestHandler)new RenderPageRequestHandler(new PageProvider(interceptPage),RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT));
        InterceptData.set();
    }
    public RestartResponseAtInterceptPageException(final Class<? extends Page> interceptPageClass){
        this(interceptPageClass,null);
    }
    public RestartResponseAtInterceptPageException(final Class<? extends Page> interceptPageClass,final PageParameters parameters){
        super((IRequestHandler)new RenderPageRequestHandler(new PageProvider(interceptPageClass,parameters),RenderPageRequestHandler.RedirectPolicy.ALWAYS_REDIRECT));
        InterceptData.set();
    }
    static boolean continueToOriginalDestination(){
        final InterceptData data=InterceptData.get();
        if(data!=null){
            data.continueOk=true;
            final String url=RequestCycle.get().getUrlRenderer().renderUrl(data.originalUrl);
            RequestCycle.get().replaceAllRequestHandlers((IRequestHandler)new RedirectRequestHandler(url));
            return true;
        }
        return false;
    }
    static{
        RestartResponseAtInterceptPageException.MAPPER=(IRequestMapper)new IRequestMapper(){
            public int getCompatibilityScore(final Request request){
                return (this.matchedData(request)!=null)?Integer.MAX_VALUE:0;
            }
            public Url mapHandler(final IRequestHandler requestHandler){
                return null;
            }
            public IRequestHandler mapRequest(final Request request){
                final InterceptData data=this.matchedData(request);
                if(data!=null){
                    if(!data.postParameters.isEmpty()&&request.getPostParameters() instanceof IWritableRequestParameters){
                        final IWritableRequestParameters parameters=(IWritableRequestParameters)request.getPostParameters();
                        parameters.reset();
                        for(final String s : data.postParameters.keySet()){
                            parameters.setParameterValues(s,(List)data.postParameters.get(s));
                        }
                    }
                    InterceptData.clear();
                }
                return null;
            }
            private InterceptData matchedData(final Request request){
                final InterceptData data=InterceptData.get();
                if(data!=null&&data.originalUrl.equals((Object)request.getOriginalUrl())){
                    return data;
                }
                return null;
            }
        };
    }
    static class InterceptData implements Serializable{
        private static final long serialVersionUID=1L;
        private Url originalUrl;
        private Map<String,List<StringValue>> postParameters;
        private boolean continueOk;
        private static MetaDataKey<InterceptData> key;
        public Url getOriginalUrl(){
            return this.originalUrl;
        }
        public Map<String,List<StringValue>> getPostParameters(){
            return this.postParameters;
        }
        public static void set(){
            final Session session=Session.get();
            session.bind();
            final InterceptData data=new InterceptData();
            final Request request=RequestCycle.get().getRequest();
            data.originalUrl=request.getOriginalUrl();
            final Iterator<Url.QueryParameter> itor=(Iterator<Url.QueryParameter>)data.originalUrl.getQueryParameters().iterator();
            while(itor.hasNext()){
                final Url.QueryParameter parameter=(Url.QueryParameter)itor.next();
                final String parameterName=parameter.getName();
                if("wicket-ajax".equals(parameterName)||"wicket-ajax-baseurl".equals(parameterName)||"random".equals(parameterName)){
                    itor.remove();
                }
            }
            data.postParameters=(Map<String,List<StringValue>>)new HashMap();
            for(final String s : request.getPostParameters().getParameterNames()){
                if(!"wicket-ajax".equals(s)&&!"wicket-ajax-baseurl".equals(s)){
                    if("random".equals(s)){
                        continue;
                    }
                    data.postParameters.put(s,new ArrayList(request.getPostParameters().getParameterValues(s)));
                }
            }
            data.continueOk=false;
            session.setMetaData(InterceptData.key,data);
        }
        public static InterceptData get(){
            return Session.get().getMetaData(InterceptData.key);
        }
        public static void clear(){
            if(Session.exists()){
                Session.get().setMetaData(InterceptData.key,null);
            }
        }
        static{
            InterceptData.key=new MetaDataKey<InterceptData>(){
                private static final long serialVersionUID=1L;
            };
        }
    }
}
