package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.response.filter.*;
import org.apache.wicket.util.time.*;
import java.util.*;
import org.apache.wicket.util.lang.*;

public class RequestCycleSettings implements IRequestCycleSettings{
    private boolean bufferResponse;
    private boolean gatherExtendedBrowserInfo;
    private IExceptionSettings.UnexpectedExceptionDisplay unexpectedExceptionDisplay;
    private RenderStrategy renderStrategy;
    private List<IResponseFilter> responseFilters;
    private String responseRequestEncoding;
    private Duration timeout;
    public RequestCycleSettings(){
        super();
        this.bufferResponse=true;
        this.gatherExtendedBrowserInfo=false;
        this.unexpectedExceptionDisplay=IExceptionSettings.SHOW_EXCEPTION_PAGE;
        this.renderStrategy=RenderStrategy.REDIRECT_TO_BUFFER;
        this.responseRequestEncoding="UTF-8";
        this.timeout=Duration.ONE_MINUTE;
    }
    public void addResponseFilter(final IResponseFilter responseFilter){
        if(this.responseFilters==null){
            this.responseFilters=(List<IResponseFilter>)new ArrayList(3);
        }
        this.responseFilters.add(responseFilter);
    }
    public boolean getBufferResponse(){
        return this.bufferResponse;
    }
    public boolean getGatherExtendedBrowserInfo(){
        return this.gatherExtendedBrowserInfo;
    }
    public RenderStrategy getRenderStrategy(){
        return this.renderStrategy;
    }
    public List<IResponseFilter> getResponseFilters(){
        if(this.responseFilters==null){
            return null;
        }
        return (List<IResponseFilter>)Collections.unmodifiableList(this.responseFilters);
    }
    public String getResponseRequestEncoding(){
        return this.responseRequestEncoding;
    }
    public Duration getTimeout(){
        return this.timeout;
    }
    public IExceptionSettings.UnexpectedExceptionDisplay getUnexpectedExceptionDisplay(){
        return this.unexpectedExceptionDisplay;
    }
    public void setBufferResponse(final boolean bufferResponse){
        this.bufferResponse=bufferResponse;
    }
    public void setGatherExtendedBrowserInfo(final boolean gatherExtendedBrowserInfo){
        this.gatherExtendedBrowserInfo=gatherExtendedBrowserInfo;
    }
    public void setRenderStrategy(final RenderStrategy renderStrategy){
        this.renderStrategy=renderStrategy;
    }
    public void setResponseRequestEncoding(final String encoding){
        Args.notNull((Object)encoding,"encoding");
        this.responseRequestEncoding=encoding;
    }
    public void setTimeout(final Duration timeout){
        if(timeout==null){
            throw new IllegalArgumentException("timeout cannot be null");
        }
        this.timeout=timeout;
    }
    public void setUnexpectedExceptionDisplay(final IExceptionSettings.UnexpectedExceptionDisplay unexpectedExceptionDisplay){
        this.unexpectedExceptionDisplay=unexpectedExceptionDisplay;
    }
}
