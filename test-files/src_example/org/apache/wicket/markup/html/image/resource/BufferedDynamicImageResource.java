package org.apache.wicket.markup.html.image.resource;

import java.awt.image.*;
import org.apache.wicket.request.resource.*;

public class BufferedDynamicImageResource extends DynamicImageResource{
    private static final long serialVersionUID=1L;
    private byte[] imageData;
    public BufferedDynamicImageResource(){
        super();
    }
    public BufferedDynamicImageResource(final String format){
        super(format);
    }
    public synchronized void setImage(final BufferedImage image){
        this.imageData=this.toImageData(image);
    }
    protected byte[] getImageData(final IResource.Attributes attributes){
        return this.imageData;
    }
}
