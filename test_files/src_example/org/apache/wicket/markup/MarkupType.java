package org.apache.wicket.markup;

import java.io.*;

public class MarkupType implements Serializable{
    private static final long serialVersionUID=1L;
    public static final String XML_MIME="application/xhtml+xml";
    public static final String HTML_MIME="text/html";
    public static final MarkupType HTML_MARKUP_TYPE;
    private final String extension;
    private final String mimeType;
    public MarkupType(final String extension,final String mimeType){
        super();
        this.extension=extension;
        this.mimeType=mimeType;
    }
    public final String getExtension(){
        return this.extension;
    }
    public final String getMimeType(){
        return this.mimeType;
    }
    public String toString(){
        return "MarkupType [extension="+this.extension+", mimeType="+this.mimeType+"]";
    }
    static{
        HTML_MARKUP_TYPE=new MarkupType("html","text/html");
    }
}
