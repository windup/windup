package org.apache.wicket.request.resource;

import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import org.slf4j.*;

public class ContextRelativeResource extends AbstractResource implements IStaticCacheableResource{
    private static final String CACHE_PREFIX="context-relative:/";
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private final String path;
    public ContextRelativeResource(String pathRelativeToContextRoot){
        super();
        if(pathRelativeToContextRoot==null){
            throw new IllegalArgumentException("Cannot have null path for ContextRelativeResource.");
        }
        if(!pathRelativeToContextRoot.startsWith("/")){
            pathRelativeToContextRoot="/"+pathRelativeToContextRoot;
        }
        this.path=pathRelativeToContextRoot;
    }
    public Serializable getCacheKey(){
        return "context-relative:/"+this.path;
    }
    public IResourceStream getCacheableResourceStream(){
        return (IResourceStream)new WebExternalResourceStream(this.path);
    }
    protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
        final ResourceResponse resourceResponse=new ResourceResponse();
        final WebExternalResourceStream webExternalResourceStream=new WebExternalResourceStream(this.path);
        resourceResponse.setContentType(webExternalResourceStream.getContentType());
        resourceResponse.setLastModified(webExternalResourceStream.lastModifiedTime());
        resourceResponse.setFileName(this.path);
        resourceResponse.setWriteCallback(new WriteCallback(){
            public void writeData(final IResource.Attributes attributes){
                InputStream inputStream=null;
                final ByteArrayOutputStream baos=new ByteArrayOutputStream();
                try{
                    inputStream=webExternalResourceStream.getInputStream();
                    Streams.copy(inputStream,(OutputStream)baos);
                    attributes.getResponse().write(baos.toByteArray());
                }
                catch(ResourceStreamNotFoundException rsnfx){
                    throw new WicketRuntimeException((Throwable)rsnfx);
                }
                catch(IOException iox){
                    throw new WicketRuntimeException(iox);
                }
                finally{
                    IOUtils.closeQuietly((Closeable)inputStream);
                    IOUtils.closeQuietly((Closeable)baos);
                }
            }
        });
        return resourceResponse;
    }
    public int hashCode(){
        final int prime=31;
        int result=1;
        result=31*result+((this.path==null)?0:this.path.hashCode());
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
        final ContextRelativeResource other=(ContextRelativeResource)obj;
        if(this.path==null){
            if(other.path!=null){
                return false;
            }
        }
        else if(!this.path.equals(other.path)){
            return false;
        }
        return true;
    }
    static{
        log=LoggerFactory.getLogger(ContextRelativeResource.class);
    }
}
