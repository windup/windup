package org.apache.wicket.response;

import org.apache.wicket.request.*;
import org.apache.wicket.*;
import java.io.*;

public class ByteArrayResponse extends Response{
    private ByteArrayOutputStream bytes;
    private Response original;
    public ByteArrayResponse(final Response original){
        super();
        this.original=original;
        this.reset();
    }
    public ByteArrayResponse(){
        this(null);
    }
    public byte[] getBytes(){
        return this.bytes.toByteArray();
    }
    public void write(final CharSequence string){
        try{
            this.bytes.write(string.toString().getBytes());
        }
        catch(IOException e){
            throw new WicketRuntimeException("Cannot write into internal byte stream",e);
        }
    }
    public void reset(){
        this.bytes=new ByteArrayOutputStream();
    }
    public void write(final byte[] array){
        try{
            this.bytes.write(array);
        }
        catch(IOException e){
            throw new WicketRuntimeException("Cannot write into internal byte stream",e);
        }
    }
    public void write(final byte[] array,final int offset,final int length){
        try{
            this.bytes.write(array,offset,length);
        }
        catch(Exception e){
            throw new WicketRuntimeException("Cannot write into internal byte stream",e);
        }
    }
    public String encodeURL(final CharSequence url){
        if(this.original!=null){
            return this.original.encodeURL(url);
        }
        return (url!=null)?url.toString():null;
    }
    public Object getContainerResponse(){
        return this.original.getContainerResponse();
    }
}
