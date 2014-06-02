package org.apache.wicket.response;

import org.apache.wicket.request.*;

public class NullResponse extends Response{
    private static final NullResponse instance;
    public static final NullResponse getInstance(){
        return NullResponse.instance;
    }
    public void write(final CharSequence string){
    }
    public void write(final byte[] array){
    }
    public void write(final byte[] array,final int offset,final int length){
    }
    public String encodeURL(final CharSequence url){
        return (url!=null)?url.toString():null;
    }
    public Object getContainerResponse(){
        return null;
    }
    static{
        instance=new NullResponse();
    }
}
