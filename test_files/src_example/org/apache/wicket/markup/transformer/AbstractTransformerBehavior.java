package org.apache.wicket.markup.transformer;

import org.apache.wicket.behavior.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.*;

public abstract class AbstractTransformerBehavior extends Behavior implements ITransformer{
    private static final long serialVersionUID=1L;
    private transient Response originalResponse;
    protected BufferedWebResponse newResponse(final WebResponse originalResponse){
        return new BufferedWebResponse(originalResponse);
    }
    public void beforeRender(final Component component){
        super.beforeRender(component);
        final RequestCycle requestCycle=RequestCycle.get();
        this.originalResponse=requestCycle.getResponse();
        final WebResponse origResponse=(this.originalResponse instanceof WebResponse)?this.originalResponse:null;
        final BufferedWebResponse tempResponse=this.newResponse(origResponse);
        requestCycle.setResponse((Response)tempResponse);
    }
    public void afterRender(final Component component){
        final RequestCycle requestCycle=RequestCycle.get();
        try{
            final BufferedWebResponse tempResponse=(BufferedWebResponse)requestCycle.getResponse();
            final CharSequence output=this.transform(component,tempResponse.getText());
            this.originalResponse.write(output);
        }
        catch(Exception ex){
            throw new WicketRuntimeException("Error while transforming the output of component: "+component,ex);
        }
        finally{
            requestCycle.setResponse(this.originalResponse);
        }
    }
    public void detach(final Component component){
        this.originalResponse=null;
        super.detach(component);
    }
    public abstract CharSequence transform(final Component p0,final CharSequence p1) throws Exception;
}
