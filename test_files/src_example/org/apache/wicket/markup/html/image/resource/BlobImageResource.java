package org.apache.wicket.markup.html.image.resource;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.*;
import java.sql.*;
import java.io.*;

public abstract class BlobImageResource extends DynamicImageResource{
    private static final long serialVersionUID=1L;
    public BlobImageResource(final String format){
        super(format);
    }
    public BlobImageResource(){
        super();
    }
    protected byte[] getImageData(final IResource.Attributes attributes){
        try{
            Blob blob=this.getBlob(attributes);
            if(blob==null){
                blob=this.getBlob();
            }
            if(blob!=null){
                final InputStream in=blob.getBinaryStream();
                final ByteArrayOutputStream out=new ByteArrayOutputStream();
                Streams.copy(in,(OutputStream)out);
                return out.toByteArray();
            }
            return new byte[0];
        }
        catch(SQLException e){
            throw new WicketRuntimeException("Error while reading image data",e);
        }
        catch(IOException e2){
            throw new WicketRuntimeException("Error while reading image data",e2);
        }
    }
    @Deprecated
    protected abstract Blob getBlob();
    protected Blob getBlob(final IResource.Attributes attributes){
        return null;
    }
}
