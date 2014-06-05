package org.apache.wicket.markup.html.image.resource;

import java.lang.ref.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.time.*;
import java.awt.image.*;
import java.awt.*;

public abstract class RenderedDynamicImageResource extends DynamicImageResource{
    private static final long serialVersionUID=1L;
    private int height;
    private transient SoftReference<byte[]> imageData;
    private int type;
    private int width;
    public RenderedDynamicImageResource(final int width,final int height){
        super();
        this.height=100;
        this.type=1;
        this.width=100;
        this.width=width;
        this.height=height;
    }
    public RenderedDynamicImageResource(final int width,final int height,final String format){
        super(format);
        this.height=100;
        this.type=1;
        this.width=100;
        this.width=width;
        this.height=height;
    }
    public synchronized int getHeight(){
        return this.height;
    }
    public synchronized int getType(){
        return this.type;
    }
    public synchronized int getWidth(){
        return this.width;
    }
    public synchronized void invalidate(){
        this.imageData=null;
    }
    public synchronized void setHeight(final int height){
        this.height=height;
        this.invalidate();
    }
    public synchronized void setType(final int type){
        this.type=type;
        this.invalidate();
    }
    public synchronized void setWidth(final int width){
        this.width=width;
        this.invalidate();
    }
    protected byte[] getImageData(final IResource.Attributes attributes){
        byte[] data=null;
        if(this.imageData!=null){
            data=(byte[])this.imageData.get();
        }
        if(data==null){
            data=this.render(attributes);
            this.imageData=(SoftReference<byte[]>)new SoftReference(data);
            this.setLastModifiedTime(Time.now());
        }
        return data;
    }
    @Deprecated
    protected byte[] render(){
        return this.render((IResource.Attributes)null);
    }
    protected byte[] render(final IResource.Attributes attributes){
        BufferedImage image;
        do{
            image=new BufferedImage(this.getWidth(),this.getHeight(),this.getType());
        } while(!this.render((Graphics2D)image.getGraphics(),attributes));
        return this.toImageData(image);
    }
    @Deprecated
    protected boolean render(final Graphics2D graphics){
        return true;
    }
    protected boolean render(final Graphics2D graphics,final IResource.Attributes attributes){
        return this.render(graphics);
    }
}
