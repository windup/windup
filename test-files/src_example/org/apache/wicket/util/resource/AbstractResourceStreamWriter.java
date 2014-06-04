package org.apache.wicket.util.resource;

import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.time.*;
import java.io.*;

public abstract class AbstractResourceStreamWriter implements IResourceStreamWriter{
    private static final long serialVersionUID=1L;
    private Locale locale;
    private String variation;
    private String style;
    public Bytes length(){
        return null;
    }
    public Locale getLocale(){
        return this.locale;
    }
    public void setLocale(final Locale locale){
        this.locale=locale;
    }
    public Time lastModifiedTime(){
        return Time.now();
    }
    public final InputStream getInputStream() throws ResourceStreamNotFoundException{
        throw new IllegalStateException("getInputStream is not used with IResourceStreamWriter");
    }
    public final void close() throws IOException{
    }
    public String getContentType(){
        return null;
    }
    public String getStyle(){
        return this.style;
    }
    public void setStyle(final String style){
        this.style=style;
    }
    public String getVariation(){
        return this.variation;
    }
    public void setVariation(final String variation){
        this.variation=variation;
    }
}
