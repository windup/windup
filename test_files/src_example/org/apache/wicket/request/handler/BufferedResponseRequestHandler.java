package org.apache.wicket.request.handler;

import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.http.*;

public class BufferedResponseRequestHandler implements IRequestHandler{
    private final BufferedWebResponse bufferedWebResponse;
    public BufferedResponseRequestHandler(final BufferedWebResponse bufferedWebResponse){
        super();
        this.bufferedWebResponse=bufferedWebResponse;
    }
    public void detach(final IRequestCycle requestCycle){
    }
    public void respond(final IRequestCycle requestCycle){
        this.bufferedWebResponse.writeTo((WebResponse)requestCycle.getResponse());
    }
}
