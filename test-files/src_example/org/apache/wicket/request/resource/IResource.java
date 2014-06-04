package org.apache.wicket.request.resource;

import org.apache.wicket.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.lang.*;

public interface IResource extends IClusterable{
    void respond(Attributes p0);
    public static class Attributes{
        private final Request request;
        private final Response response;
        private final PageParameters parameters;
        public Attributes(Request request,Response response,PageParameters parameters){
            super();
            Args.notNull((Object)request,"request");
            Args.notNull((Object)response,"response");
            this.request=request;
            this.response=response;
            this.parameters=parameters;
        }
        public Attributes(Request request,Response response){
            this(request,response,null);
        }
        public Request getRequest(){
            return this.request;
        }
        public Response getResponse(){
            return this.response;
        }
        public PageParameters getParameters(){
            return this.parameters;
        }
    }
}
