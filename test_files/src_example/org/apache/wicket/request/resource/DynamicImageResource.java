package org.apache.wicket.request.resource;

import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;
import javax.imageio.*;
import java.awt.image.*;
import org.apache.wicket.*;
import java.io.*;

public abstract class DynamicImageResource extends AbstractResource{
    private static final long serialVersionUID=1L;
    private String format;
    private Time lastModifiedTime;
    public DynamicImageResource(){
        super();
        this.format="png";
    }
    public DynamicImageResource(final String format){
        super();
        this.format="png";
        this.setFormat(format);
    }
    public final synchronized String getFormat(){
        return this.format;
    }
    public final synchronized void setFormat(final String format){
        Args.notNull((Object)format,"format");
        this.format=format;
    }
    protected synchronized void setLastModifiedTime(final Time time){
        this.lastModifiedTime=time;
    }
    protected byte[] toImageData(final BufferedImage image){
        try{
            final ByteArrayOutputStream out=new ByteArrayOutputStream();
            ImageIO.write(image,this.format,out);
            return out.toByteArray();
        }
        catch(IOException e){
            throw new WicketRuntimeException("Unable to convert dynamic image to stream",e);
        }
    }
    protected abstract byte[] getImageData(final IResource.Attributes p0);
    protected void configureResponse(final ResourceResponse response,final IResource.Attributes attributes){
    }
    protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
        final ResourceResponse response=new ResourceResponse();
        if(this.lastModifiedTime!=null){
            response.setLastModified(this.lastModifiedTime);
        }
        else{
            response.setLastModified(Time.now());
        }
        if(response.dataNeedsToBeWritten(attributes)){
            response.setContentType("image/"+this.getFormat());
            response.setContentDisposition(ContentDisposition.INLINE);
            final byte[] imageData=this.getImageData(attributes);
            if(imageData==null){
                response.setError(404);
            }
            else{
                response.setWriteCallback(new WriteCallback(){
                    public void writeData(final IResource.Attributes attributes){
                        attributes.getResponse().write(imageData);
                    }
                });
                this.configureResponse(response,attributes);
            }
        }
        return response;
    }
}
