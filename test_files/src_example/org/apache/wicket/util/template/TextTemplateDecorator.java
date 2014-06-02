package org.apache.wicket.util.template;

import org.apache.wicket.util.lang.*;
import java.io.*;
import org.apache.wicket.util.resource.*;
import java.util.*;
import org.apache.wicket.util.time.*;
import java.nio.charset.*;

public abstract class TextTemplateDecorator extends TextTemplate{
    private static final long serialVersionUID=1L;
    protected final TextTemplate decorated;
    public TextTemplateDecorator(final TextTemplate textTemplate){
        super();
        Args.notNull((Object)textTemplate,"textTemplate");
        this.decorated=textTemplate;
    }
    public String asString(){
        return this.asString((Map<String,?>)Collections.emptyMap());
    }
    public String asString(final Map<String,?> variables){
        final StringBuilder b=new StringBuilder();
        b.append(this.getBeforeTemplateContents());
        b.append(this.decorated.asString(variables));
        b.append(this.getAfterTemplateContents());
        return b.toString();
    }
    public abstract String getBeforeTemplateContents();
    public abstract String getAfterTemplateContents();
    public void close() throws IOException{
        this.decorated.close();
    }
    public boolean equals(final Object obj){
        return this.decorated.equals(obj);
    }
    public String getContentType(){
        return this.decorated.getContentType();
    }
    public InputStream getInputStream() throws ResourceStreamNotFoundException{
        return this.decorated.getInputStream();
    }
    public Locale getLocale(){
        return this.decorated.getLocale();
    }
    public int hashCode(){
        return this.decorated.hashCode();
    }
    public Time lastModifiedTime(){
        return this.decorated.lastModifiedTime();
    }
    public void setCharset(final Charset charset){
        this.decorated.setCharset(charset);
    }
    public void setLastModified(final Time lastModified){
        this.decorated.setLastModified(lastModified);
    }
    public void setLocale(final Locale locale){
        this.decorated.setLocale(locale);
    }
    public String getString(){
        return this.decorated.getString();
    }
    public String toString(){
        return this.decorated.toString();
    }
}
