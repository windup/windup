package org.apache.wicket.markup;

import java.io.*;
import org.apache.wicket.util.resource.*;
import java.util.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;
import java.util.regex.*;
import org.slf4j.*;

public class MarkupResourceStream implements IResourceStream,IFixedLocationResourceStream{
    private static final long serialVersionUID=1846489965076612828L;
    private static final Logger log;
    public static final String WICKET_XHTML_DTD="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd";
    private static final Pattern DOCTYPE_REGEX;
    private final IResourceStream resourceStream;
    private final transient ContainerInfo containerInfo;
    private final String markupClassName;
    private String cacheKey;
    private transient Markup baseMarkup;
    private String encoding;
    private String wicketNamespace;
    private String wicketId;
    private String doctype;
    public MarkupResourceStream(final IResourceStream resourceStream){
        this(resourceStream,null,null);
    }
    public MarkupResourceStream(final IResourceStream resourceStream,final ContainerInfo containerInfo,final Class<?> markupClass){
        super();
        this.resourceStream=resourceStream;
        this.containerInfo=containerInfo;
        this.markupClassName=((markupClass==null)?null:markupClass.getName());
        if(resourceStream==null){
            throw new IllegalArgumentException("Parameter 'resourceStream' must not be null");
        }
        this.setWicketNamespace("wicket");
    }
    public String locationAsString(){
        if(this.resourceStream instanceof IFixedLocationResourceStream){
            return ((IFixedLocationResourceStream)this.resourceStream).locationAsString();
        }
        return null;
    }
    public void close() throws IOException{
        this.resourceStream.close();
    }
    public String getContentType(){
        return this.resourceStream.getContentType();
    }
    public InputStream getInputStream() throws ResourceStreamNotFoundException{
        return this.resourceStream.getInputStream();
    }
    public Locale getLocale(){
        return this.resourceStream.getLocale();
    }
    public Time lastModifiedTime(){
        return this.resourceStream.lastModifiedTime();
    }
    public Bytes length(){
        return this.resourceStream.length();
    }
    public void setLocale(final Locale locale){
        this.resourceStream.setLocale(locale);
    }
    public Class<? extends Component> getMarkupClass(){
        return WicketObjects.resolveClass(this.markupClassName);
    }
    public ContainerInfo getContainerInfo(){
        return this.containerInfo;
    }
    public final String getCacheKey(){
        return this.cacheKey;
    }
    public final void setCacheKey(final String cacheKey){
        this.cacheKey=cacheKey;
    }
    public IResourceStream getResource(){
        return this.resourceStream;
    }
    public String getEncoding(){
        return this.encoding;
    }
    public String getWicketNamespace(){
        return this.wicketNamespace;
    }
    public final String getWicketId(){
        return this.wicketId;
    }
    final void setEncoding(final String encoding){
        this.encoding=encoding;
    }
    public final void setWicketNamespace(final String wicketNamespace){
        this.wicketNamespace=wicketNamespace;
        this.wicketId=wicketNamespace+":id";
        if(!"wicket".equals(wicketNamespace)){
            MarkupResourceStream.log.debug("You are using a non-standard namespace name: '{}'",wicketNamespace);
        }
    }
    public MarkupResourceStream getBaseMarkupResourceStream(){
        if(this.baseMarkup==null){
            return null;
        }
        return this.baseMarkup.getMarkupResourceStream();
    }
    public void setBaseMarkup(final Markup baseMarkup){
        this.baseMarkup=baseMarkup;
    }
    public Markup getBaseMarkup(){
        return this.baseMarkup;
    }
    public String getStyle(){
        return this.resourceStream.getStyle();
    }
    public String getVariation(){
        return this.resourceStream.getVariation();
    }
    public void setStyle(final String style){
        this.resourceStream.setStyle(style);
    }
    public void setVariation(final String variation){
        this.resourceStream.setVariation(variation);
    }
    public String toString(){
        if(this.resourceStream!=null){
            return this.resourceStream.toString();
        }
        return "(unknown resource)";
    }
    public final String getDoctype(){
        if(this.doctype==null){
            final MarkupResourceStream baseMarkupResourceStream=this.getBaseMarkupResourceStream();
            if(baseMarkupResourceStream!=null){
                this.doctype=baseMarkupResourceStream.getDoctype();
            }
        }
        return this.doctype;
    }
    public final void setDoctype(final CharSequence doctype){
        if(!Strings.isEmpty(doctype)){
            String doc=doctype.toString().replaceAll("[\n\r]+","");
            doc=doc.replaceAll("\\s+"," ");
            final Matcher matcher=MarkupResourceStream.DOCTYPE_REGEX.matcher((CharSequence)doc);
            if(!matcher.matches()){
                throw new MarkupException("Invalid DOCTYPE: '"+(Object)doctype+"'");
            }
            this.doctype=matcher.group(1).trim();
        }
    }
    public boolean isHtml5(){
        return "html".equalsIgnoreCase(this.getDoctype());
    }
    static{
        log=LoggerFactory.getLogger(MarkupResourceStream.class);
        DOCTYPE_REGEX=Pattern.compile("!DOCTYPE\\s+(.*)\\s*");
    }
}
