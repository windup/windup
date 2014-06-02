package org.apache.wicket.settings;

import org.apache.wicket.response.filter.*;
import java.util.*;
import org.apache.wicket.util.time.*;

public interface IRequestCycleSettings{
    void addResponseFilter(IResponseFilter p0);
    boolean getBufferResponse();
    boolean getGatherExtendedBrowserInfo();
    RenderStrategy getRenderStrategy();
    List<IResponseFilter> getResponseFilters();
    String getResponseRequestEncoding();
    Duration getTimeout();
    @Deprecated
    IExceptionSettings.UnexpectedExceptionDisplay getUnexpectedExceptionDisplay();
    void setBufferResponse(boolean p0);
    void setGatherExtendedBrowserInfo(boolean p0);
    void setRenderStrategy(RenderStrategy p0);
    void setResponseRequestEncoding(String p0);
    void setTimeout(Duration p0);
    @Deprecated
    void setUnexpectedExceptionDisplay(IExceptionSettings.UnexpectedExceptionDisplay p0);
    public enum RenderStrategy{
        ONE_PASS_RENDER,REDIRECT_TO_BUFFER,REDIRECT_TO_RENDER;
    }
}
