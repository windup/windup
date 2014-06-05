package org.apache.wicket.request.resource;

import org.apache.wicket.util.time.*;
import java.net.*;

public class ByteArrayResource extends AbstractResource{
    private static final long serialVersionUID=1L;
    private final String contentType;
    private byte[] array;
    private final Time lastModified;
    private final String filename;
    public ByteArrayResource(final String contentType){
        this(contentType,null,null);
    }
    public ByteArrayResource(final String contentType,final byte[] array){
        this(contentType,array,null);
    }
    public ByteArrayResource(final String contentType,final byte[] array,final String filename){
        super();
        this.lastModified=Time.now();
        this.contentType=contentType;
        this.array=array;
        this.filename=filename;
    }
    protected void configureResponse(final ResourceResponse response,final IResource.Attributes attributes){
    }
    protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
        final ResourceResponse response=new ResourceResponse();
        String contentType=this.contentType;
        if(contentType==null){
            if(this.filename!=null){
                contentType=URLConnection.getFileNameMap().getContentTypeFor(this.filename);
            }
            if(contentType==null){
                contentType="application/octet-stream";
            }
        }
        response.setContentType(contentType);
        response.setLastModified(this.lastModified);
        final byte[] data=this.getData(attributes);
        if(data==null){
            response.setError(404);
        }
        else{
            response.setContentLength(data.length);
            if(response.dataNeedsToBeWritten(attributes)){
                if(this.filename!=null){
                    response.setFileName(this.filename);
                    response.setContentDisposition(ContentDisposition.ATTACHMENT);
                }
                else{
                    response.setContentDisposition(ContentDisposition.INLINE);
                }
                response.setWriteCallback(new WriteCallback(){
                    public void writeData(final IResource.Attributes attributes){
                        attributes.getResponse().write(data);
                    }
                });
                this.configureResponse(response,attributes);
            }
        }
        return response;
    }
    protected byte[] getData(final IResource.Attributes attributes){
        return this.array;
    }
}
