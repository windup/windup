package org.apache.wicket.util.resource;

import org.apache.wicket.util.time.*;
import org.apache.wicket.*;
import java.net.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.slf4j.*;

public class UrlResourceStream extends AbstractResourceStream implements IFixedLocationResourceStream{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private transient StreamData streamData;
    private final URL url;
    private Time lastModified;
    public UrlResourceStream(final URL url){
        super();
        this.url=(URL)Args.notNull((Object)url,"url");
    }
    private StreamData getData(final boolean initialize){
        if(this.streamData==null&&initialize){
            this.streamData=new StreamData();
            try{
                this.streamData.connection=this.url.openConnection();
                this.streamData.contentLength=this.streamData.connection.getContentLength();
                this.streamData.contentType=this.streamData.connection.getContentType();
                if(this.streamData.contentType==null||this.streamData.contentType.contains((CharSequence)"unknown")){
                    if(Application.exists()){
                        this.streamData.contentType=Application.get().getMimeType(this.url.getFile());
                    }
                    else{
                        this.streamData.contentType=URLConnection.getFileNameMap().getContentTypeFor(this.url.getFile());
                    }
                }
            }
            catch(IOException ex){
                throw new IllegalArgumentException("Invalid URL parameter "+this.url,(Throwable)ex);
            }
        }
        return this.streamData;
    }
    public void close() throws IOException{
        final StreamData data=this.getData(false);
        if(data!=null){
            Connections.closeQuietly(data.connection);
            if(data.inputStreams!=null){
                for(final InputStream is : data.inputStreams){
                    IOUtils.closeQuietly((Closeable)is);
                }
            }
            this.streamData=null;
        }
    }
    public InputStream getInputStream() throws ResourceStreamNotFoundException{
        try{
            final StreamData data=this.getData(true);
            final InputStream is=data.connection.getInputStream();
            if(data.inputStreams==null){
                data.inputStreams=(List<InputStream>)new ArrayList();
            }
            data.inputStreams.add(is);
            return is;
        }
        catch(IOException e){
            throw new ResourceStreamNotFoundException("Resource "+this.url+" could not be opened",(Throwable)e);
        }
    }
    public URL getURL(){
        return this.url;
    }
    public Time lastModifiedTime(){
        try{
            final Time time=Connections.getLastModified(this.url);
            if(!Objects.equal((Object)time,(Object)this.lastModified)){
                this.lastModified=time;
                this.updateContentLength();
            }
            return this.lastModified;
        }
        catch(IOException e){
            UrlResourceStream.log.warn("getLastModified for "+this.url+" failed: "+e.getMessage());
            return null;
        }
    }
    private void updateContentLength() throws IOException{
        final StreamData data=this.getData(false);
        if(data!=null){
            final URLConnection connection=this.url.openConnection();
            try{
                data.contentLength=connection.getContentLength();
            }
            finally{
                Connections.close(connection);
            }
        }
    }
    public String toString(){
        return this.url.toString();
    }
    public String getContentType(){
        return this.getData(true).contentType;
    }
    public Bytes length(){
        final long length=this.getData(true).contentLength;
        if(length==-1L){
            return null;
        }
        return Bytes.bytes(length);
    }
    public String locationAsString(){
        return this.url.toExternalForm();
    }
    static{
        log=LoggerFactory.getLogger(UrlResourceStream.class);
    }
    private static class StreamData{
        private URLConnection connection;
        private List<InputStream> inputStreams;
        private long contentLength;
        private String contentType;
    }
}
