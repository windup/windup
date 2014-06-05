package org.apache.wicket.request.handler.resource;

import org.apache.wicket.util.resource.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class WebExternalResourceRequestHandler extends ResourceStreamRequestHandler{
    private static final Logger log;
    private final String uri;
    public WebExternalResourceRequestHandler(final String uri){
        super((IResourceStream)new WebExternalResourceStream(uri));
        this.uri=uri;
        this.setContentDisposition(ContentDisposition.INLINE);
    }
    public final String getUrl(){
        return this.uri;
    }
    public void detach(final IRequestCycle requestCycle){
    }
    public boolean equals(final Object obj){
        if(obj instanceof WebExternalResourceRequestHandler){
            final WebExternalResourceRequestHandler that=(WebExternalResourceRequestHandler)obj;
            return this.uri.equals(that.uri);
        }
        return false;
    }
    public int hashCode(){
        int result="WebExternalResourceRequestTarget".hashCode();
        result+=this.uri.hashCode();
        return 17*result;
    }
    public String toString(){
        return "[WebExternalResourceRequestTarget@"+this.hashCode()+" "+this.uri+"]";
    }
    static{
        log=LoggerFactory.getLogger(WebExternalResourceRequestHandler.class);
    }
}
