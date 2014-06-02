package org.apache.wicket.response;

import org.apache.wicket.request.*;
import org.apache.wicket.util.string.*;

public class StringResponse extends Response{
    protected final AppendingStringBuffer out;
    public StringResponse(){
        super();
        this.out=new AppendingStringBuffer(128);
    }
    public void write(final CharSequence string){
        this.out.append((Object)string);
    }
    public void reset(){
        this.out.clear();
    }
    public String toString(){
        return this.out.toString();
    }
    public CharSequence getBuffer(){
        return (CharSequence)this.out;
    }
    public void write(final byte[] array){
        throw new UnsupportedOperationException();
    }
    public void write(final byte[] array,final int offset,final int length){
        throw new UnsupportedOperationException();
    }
    public String encodeURL(final CharSequence url){
        return (url!=null)?url.toString():null;
    }
    public Object getContainerResponse(){
        return null;
    }
}
