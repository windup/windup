package org.apache.wicket.util.resource;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.*;
import java.io.*;
import org.apache.wicket.util.io.*;
import javax.servlet.*;
import java.net.*;
import org.slf4j.*;

public class WebExternalResourceStream extends AbstractResourceStream{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    transient InputStream in;
    private final String url;
    public WebExternalResourceStream(final String url){
        super();
        if(url==null){
            throw new IllegalArgumentException("Argument url must be not null");
        }
        this.url=url;
    }
    public Bytes length(){
        return null;
    }
    public void close() throws IOException{
        IOUtils.close((Closeable)this.in);
    }
    public Time lastModifiedTime(){
        try{
            final ServletContext context=((WebApplication)Application.get()).getServletContext();
            final URL resourceURL=context.getResource(this.url);
            if(resourceURL==null){
                throw new FileNotFoundException("Unable to find resource '"+this.url+"' in the serlvet context");
            }
            return Connections.getLastModified(resourceURL);
        }
        catch(IOException e){
            WebExternalResourceStream.log.warn("failed to retrieve last modified timestamp",e);
            return null;
        }
    }
    public String getContentType(){
        return WebApplication.get().getServletContext().getMimeType(this.url);
    }
    public InputStream getInputStream() throws ResourceStreamNotFoundException{
        final ServletContext context=((WebApplication)Application.get()).getServletContext();
        this.in=context.getResourceAsStream(this.url);
        if(this.in==null){
            throw new ResourceStreamNotFoundException("The requested resource was not found: "+this.url);
        }
        return this.in;
    }
    static{
        log=LoggerFactory.getLogger(WebExternalResourceStream.class);
    }
}
