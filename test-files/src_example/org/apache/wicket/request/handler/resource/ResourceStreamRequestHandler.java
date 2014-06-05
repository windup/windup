package org.apache.wicket.request.handler.resource;

import org.apache.wicket.util.time.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class ResourceStreamRequestHandler implements IRequestHandler,ILoggableRequestHandler{
    private static final Logger log;
    private String fileName;
    private ContentDisposition contentDisposition;
    private Duration cacheDuration;
    private final IResourceStream resourceStream;
    private ResourceStreamLogData logData;
    public ResourceStreamRequestHandler(final IResourceStream resourceStream){
        this(resourceStream,null);
    }
    public ResourceStreamRequestHandler(final IResourceStream resourceStream,final String fileName){
        super();
        Args.notNull((Object)resourceStream,"resourceStream");
        this.resourceStream=resourceStream;
        this.fileName=fileName;
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=((this.getResourceStream()==null)?new ResourceStreamLogData(this):new ResourceStreamLogData(this,this.getResourceStream()));
        }
    }
    public ResourceStreamLogData getLogData(){
        return this.logData;
    }
    public final String getFileName(){
        return this.fileName;
    }
    public final IResourceStream getResourceStream(){
        return this.resourceStream;
    }
    public void respond(final IRequestCycle requestCycle){
        final IResource.Attributes attributes=new IResource.Attributes(requestCycle.getRequest(),requestCycle.getResponse());
        final ResourceStreamResource resource=new ResourceStreamResource(this.resourceStream);
        resource.setFileName(this.fileName);
        if(this.contentDisposition!=null){
            resource.setContentDisposition(this.contentDisposition);
        }
        else{
            resource.setContentDisposition(Strings.isEmpty((CharSequence)this.fileName)?ContentDisposition.INLINE:ContentDisposition.ATTACHMENT);
        }
        final Duration cacheDuration=this.getCacheDuration();
        if(cacheDuration!=null){
            resource.setCacheDuration(cacheDuration);
        }
        resource.respond(attributes);
    }
    public int hashCode(){
        final int prime=31;
        int result=1;
        result=31*result+((this.contentDisposition==null)?0:this.contentDisposition.hashCode());
        result=31*result+((this.fileName==null)?0:this.fileName.hashCode());
        result=31*result+this.resourceStream.hashCode();
        return result;
    }
    public boolean equals(final Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(this.getClass()!=obj.getClass()){
            return false;
        }
        final ResourceStreamRequestHandler other=(ResourceStreamRequestHandler)obj;
        if(this.contentDisposition!=other.contentDisposition){
            return false;
        }
        if(this.fileName==null){
            if(other.fileName!=null){
                return false;
            }
        }
        else if(!this.fileName.equals(other.fileName)){
            return false;
        }
        return this.resourceStream.equals(other.resourceStream);
    }
    public final ResourceStreamRequestHandler setFileName(final String fileName){
        this.fileName=fileName;
        return this;
    }
    public String toString(){
        return "[ResourceStreamRequestTarget[resourceStream="+this.resourceStream+",fileName="+this.fileName+", contentDisposition="+this.contentDisposition+"]";
    }
    public final ContentDisposition getContentDisposition(){
        return this.contentDisposition;
    }
    public final ResourceStreamRequestHandler setContentDisposition(final ContentDisposition contentDisposition){
        this.contentDisposition=contentDisposition;
        return this;
    }
    public Duration getCacheDuration(){
        return this.cacheDuration;
    }
    public ResourceStreamRequestHandler setCacheDuration(final Duration cacheDuration){
        this.cacheDuration=cacheDuration;
        return this;
    }
    static{
        log=LoggerFactory.getLogger(ResourceStreamRequestHandler.class);
    }
}
