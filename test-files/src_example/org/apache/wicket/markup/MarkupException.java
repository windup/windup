package org.apache.wicket.markup;

import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;

public final class MarkupException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    private transient MarkupStream markupStream;
    public MarkupException(final String message){
        super(message);
        this.markupStream=null;
    }
    public MarkupException(final IResourceStream resource,final String message){
        super(resource.toString()+": "+message);
        this.markupStream=null;
    }
    public MarkupException(final IResourceStream resource,final String message,final Throwable cause){
        super(resource.toString()+": "+message,cause);
        this.markupStream=null;
    }
    public MarkupException(final MarkupStream markupStream,final String message){
        super(message);
        this.markupStream=markupStream;
    }
    public MarkupException(final MarkupStream markupStream,final String message,final Throwable cause){
        super(message,cause);
        this.markupStream=markupStream;
    }
    public MarkupException(final Markup markup,final String message,final Throwable cause){
        this(new MarkupStream(markup).setCurrentIndex(markup.size()-1),message,cause);
    }
    public MarkupStream getMarkupStream(){
        return this.markupStream;
    }
    public void setMarkupStream(final MarkupStream markupStream){
        this.markupStream=markupStream;
    }
    public String toString(){
        return this.getMessage()+"\n MarkupStream: "+((this.markupStream==null)?"[unknown]":this.markupStream.toString());
    }
}
