package org.apache.wicket.resource.filtering;

import org.apache.wicket.*;
import org.apache.wicket.response.*;
import org.apache.wicket.markup.html.*;
import java.util.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;
import org.apache.wicket.ajax.*;
import org.slf4j.*;

public class HeaderResponseContainerFilteringHeaderResponse extends DecoratingHeaderResponse{
    private static final Logger log;
    private static final MetaDataKey<HeaderResponseContainerFilteringHeaderResponse> RESPONSE_KEY;
    private final Map<String,StringResponse> responseFilterMap;
    private IHeaderResponseFilter[] filters;
    private final String headerFilterName;
    public HeaderResponseContainerFilteringHeaderResponse(final IHeaderResponse response,final String headerFilterName,final IHeaderResponseFilter[] filters){
        super(response);
        this.responseFilterMap=(Map<String,StringResponse>)new HashMap();
        this.headerFilterName=headerFilterName;
        this.setFilters(filters);
        RequestCycle.get().setMetaData(HeaderResponseContainerFilteringHeaderResponse.RESPONSE_KEY,this);
    }
    protected void setFilters(final IHeaderResponseFilter[] filters){
        this.filters=filters;
        if(filters==null){
            return;
        }
        for(final IHeaderResponseFilter filter : filters){
            this.responseFilterMap.put(filter.getName(),new StringResponse());
        }
    }
    public static HeaderResponseContainerFilteringHeaderResponse get(){
        final RequestCycle requestCycle=RequestCycle.get();
        if(requestCycle==null){
            throw new IllegalStateException("You can only get the HeaderResponseContainerFilteringHeaderResponse when there is a RequestCycle present");
        }
        final HeaderResponseContainerFilteringHeaderResponse response=requestCycle.getMetaData(HeaderResponseContainerFilteringHeaderResponse.RESPONSE_KEY);
        if(response==null){
            throw new IllegalStateException("No HeaderResponseContainerFilteringHeaderResponse is present in the request cycle.  This may mean that you have not decorated the header response with a HeaderResponseContainerFilteringHeaderResponse.  Simply calling the HeaderResponseContainerFilteringHeaderResponse constructor sets itself on the request cycle");
        }
        return response;
    }
    public void renderJavaScriptReference(final ResourceReference reference){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(reference);
            }
        });
    }
    public void renderJavaScriptReference(final ResourceReference reference,final String id){
        this.renderJavaScriptReference(reference,null,id);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(reference,pageParameters,id);
            }
        });
    }
    public void renderJavaScriptReference(final String url){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(url);
            }
        });
    }
    public void renderJavaScriptReference(final String url,final String id){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(url,id);
            }
        });
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(url,id,defer);
            }
        });
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer,final String charset){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(url,id,defer,charset);
            }
        });
    }
    public void renderJavaScript(final CharSequence javascript,final String id){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScript(javascript,id);
            }
        });
    }
    public void renderCSSReference(final ResourceReference reference){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(reference);
            }
        });
    }
    public void renderCSSReference(final String url){
        this.forCss(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(url);
            }
        });
    }
    public void renderCSSReference(final ResourceReference reference,final String media){
        this.renderCSSReference(reference,null,media);
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(reference,pageParameters,media);
            }
        });
    }
    public void renderCSSReference(final String url,final String media){
        this.forCss(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(url,media);
            }
        });
    }
    public void renderOnDomReadyJavaScript(final String javascript){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderOnDomReadyJavaScript(javascript);
            }
        });
    }
    public void renderOnLoadJavaScript(final String javascript){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderOnLoadJavaScript(javascript);
            }
        });
    }
    public void renderOnEventJavaScript(final String target,final String event,final String javascript){
        this.forJavaScript(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderOnEventJavaScript(target,event,javascript);
            }
        });
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(reference,pageParameters,id,defer);
            }
        });
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer,final String charset){
        this.forReference(reference,new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderJavaScriptReference(reference,pageParameters,id,defer,charset);
            }
        });
    }
    public void renderCSS(final CharSequence css,final String id){
        this.forCss(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSS(css,id);
            }
        });
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media,final String condition){
        this.forCss(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(reference,pageParameters,media,condition);
            }
        });
    }
    public void renderCSSReference(final String url,final String media,final String condition){
        this.forCss(new Runnable(){
            public void run(){
                HeaderResponseContainerFilteringHeaderResponse.this.getRealResponse().renderCSSReference(url,media,condition);
            }
        });
    }
    public void close(){
        final CharSequence headerContent=this.getContent(this.headerFilterName);
        RequestCycle.get().getResponse().write(headerContent);
        super.close();
    }
    public final CharSequence getContent(final String filterName){
        if(filterName==null){
            return (CharSequence)"";
        }
        final StringResponse resp=(StringResponse)this.responseFilterMap.get(filterName);
        return (CharSequence)((resp==null)?"":resp.getBuffer());
    }
    private void forReference(final ResourceReference reference,final Runnable runnable){
        for(final IHeaderResponseFilter filter : this.filters){
            if(filter.acceptReference(reference)){
                this.run(runnable,filter);
                return;
            }
        }
        HeaderResponseContainerFilteringHeaderResponse.log.warn("A ResourceReference '{}' was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests",reference);
    }
    private void forJavaScript(final Runnable runnable){
        for(final IHeaderResponseFilter filter : this.filters){
            if(filter.acceptOtherJavaScript()){
                this.run(runnable,filter);
                return;
            }
        }
        HeaderResponseContainerFilteringHeaderResponse.log.warn("JavaScript was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests");
    }
    private void forCss(final Runnable runnable){
        for(final IHeaderResponseFilter filter : this.filters){
            if(filter.acceptOtherCss()){
                this.run(runnable,filter);
                return;
            }
        }
        HeaderResponseContainerFilteringHeaderResponse.log.warn("CSS was rendered to the filtering header response, but did not match any filters, so it was effectively lost.  Make sure that you have filters that accept every possible case or else configure a default filter that returns true to all acceptance tests");
    }
    protected final void runWithFilter(final Runnable runnable,final String filterName){
        this.run(runnable,(Response)this.responseFilterMap.get(filterName));
    }
    private void run(final Runnable runnable,final IHeaderResponseFilter filter){
        this.run(runnable,(Response)this.responseFilterMap.get(filter.getName()));
    }
    private void run(final Runnable runnable,final Response response){
        if(AjaxRequestTarget.get()!=null){
            runnable.run();
            return;
        }
        final Response original=RequestCycle.get().setResponse(response);
        try{
            runnable.run();
        }
        finally{
            RequestCycle.get().setResponse(original);
        }
    }
    static{
        log=LoggerFactory.getLogger(HeaderResponseContainerFilteringHeaderResponse.class);
        RESPONSE_KEY=new MetaDataKey<HeaderResponseContainerFilteringHeaderResponse>(){
            private static final long serialVersionUID=1L;
        };
    }
    public interface IHeaderResponseFilter{
        String getName();
        boolean acceptReference(ResourceReference p0);
        boolean acceptOtherJavaScript();
        boolean acceptOtherCss();
    }
}
