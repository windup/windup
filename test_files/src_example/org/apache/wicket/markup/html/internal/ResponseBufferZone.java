package org.apache.wicket.markup.html.internal;

import org.apache.wicket.request.cycle.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.response.*;
import org.apache.wicket.request.*;

public abstract class ResponseBufferZone{
    private final RequestCycle cycle;
    private final MarkupStream stream;
    public ResponseBufferZone(final RequestCycle cycle,final MarkupStream stream){
        super();
        this.cycle=cycle;
        this.stream=stream;
    }
    public CharSequence execute(){
        final int originalStreamPos=this.stream.getCurrentIndex();
        final Response original=this.cycle.getResponse();
        final StringResponse buffer=new StringResponse();
        this.cycle.setResponse(buffer);
        try{
            this.executeInsideBufferedZone();
            return buffer.getBuffer();
        }
        finally{
            this.cycle.setResponse(original);
            this.stream.setCurrentIndex(originalStreamPos);
        }
    }
    protected abstract void executeInsideBufferedZone();
}
