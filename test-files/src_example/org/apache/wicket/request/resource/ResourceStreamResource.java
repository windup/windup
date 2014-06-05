package org.apache.wicket.request.resource;

import org.apache.wicket.util.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;
import java.io.*;
import org.slf4j.*;

public class ResourceStreamResource extends AbstractResource{
    private static final long serialVersionUID=1L;
    private static final Logger logger;
    private IResourceStream stream;
    private String fileName;
    private ContentDisposition contentDisposition;
    private String textEncoding;
    private Duration cacheDuration;
    public ResourceStreamResource(final IResourceStream stream){
        super();
        this.contentDisposition=ContentDisposition.INLINE;
        this.stream=stream;
    }
    public ResourceStreamResource setFileName(final String fileName){
        this.fileName=fileName;
        return this;
    }
    public ResourceStreamResource setContentDisposition(final ContentDisposition contentDisposition){
        this.contentDisposition=contentDisposition;
        return this;
    }
    public ResourceStreamResource setTextEncoding(final String textEncoding){
        this.textEncoding=textEncoding;
        return this;
    }
    public Duration getCacheDuration(){
        return this.cacheDuration;
    }
    public ResourceStreamResource setCacheDuration(final Duration cacheDuration){
        this.cacheDuration=cacheDuration;
        return this;
    }
    protected IResourceStream getResourceStream(){
        return this.stream;
    }
    private IResourceStream internalGetResourceStream(){
        final IResourceStream resourceStream=this.getResourceStream();
        Checks.notNull((Object)resourceStream,"%s#getResourceStream() should not return null!",new Object[] { this.getClass().getName() });
        return resourceStream;
    }
    protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
        final IResourceStream resourceStream=this.internalGetResourceStream();
        final ResourceResponse data=new ResourceResponse();
        final Time lastModifiedTime=resourceStream.lastModifiedTime();
        if(lastModifiedTime!=null){
            data.setLastModified(lastModifiedTime);
        }
        if(this.cacheDuration!=null){
            data.setCacheDuration(this.cacheDuration);
        }
        if(data.dataNeedsToBeWritten(attributes)){
            InputStream inputStream=null;
            if(!(this.stream instanceof IResourceStreamWriter)){
                try{
                    inputStream=resourceStream.getInputStream();
                }
                catch(ResourceStreamNotFoundException e){
                    data.setError(404);
                    this.close(resourceStream);
                }
            }
            data.setContentDisposition(this.contentDisposition);
            final Bytes length=resourceStream.length();
            if(length!=null){
                data.setContentLength(length.bytes());
            }
            data.setFileName(this.fileName);
            String contentType=resourceStream.getContentType();
            if(contentType==null&&this.fileName!=null&&Application.exists()){
                contentType=Application.get().getMimeType(this.fileName);
            }
            data.setContentType(contentType);
            data.setTextEncoding(this.textEncoding);
            if(resourceStream instanceof IResourceStreamWriter){
                data.setWriteCallback(new WriteCallback(){
                    public void writeData(final IResource.Attributes attributes){
                        ((IResourceStreamWriter)resourceStream).write(attributes.getResponse());
                        ResourceStreamResource.this.close(resourceStream);
                    }
                });
            }
            else{
                final InputStream s=inputStream;
                data.setWriteCallback(new WriteCallback(){
                    public void writeData(final IResource.Attributes attributes){
                        try{
                            this.writeStream(attributes,s);
                        }
                        finally{
                            ResourceStreamResource.this.close(resourceStream);
                        }
                    }
                });
            }
        }
        return data;
    }
    private void close(final IResourceStream stream){
        try{
            stream.close();
        }
        catch(IOException e){
            ResourceStreamResource.logger.error("Couldn't close ResourceStream",e);
        }
    }
    static{
        logger=LoggerFactory.getLogger(ResourceStreamResource.class);
    }
}
