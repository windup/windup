package org.apache.wicket.request.resource;

import org.apache.wicket.util.value.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;
import java.util.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.util.time.*;
import java.io.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.*;

public abstract class AbstractResource implements IResource{
    private static final long serialVersionUID=1L;
    public static final Set<String> INTERNAL_HEADERS;
    protected abstract ResourceResponse newResourceResponse(final Attributes p0);
    protected void configureCache(final ResourceResponse data,final Attributes attributes){
        final Response response=attributes.getResponse();
        if(response instanceof WebResponse){
            final Duration duration=data.getCacheDuration();
            final WebResponse webResponse=(WebResponse)response;
            if(duration.compareTo((LongValue)Duration.NONE)>0){
                webResponse.enableCaching(duration,data.getCacheScope());
            }
            else{
                webResponse.disableCaching();
            }
        }
    }
    protected IResourceCachingStrategy getCachingStrategy(){
        return Application.get().getResourceSettings().getCachingStrategy();
    }
    public void respond(final Attributes attributes){
        final ResourceResponse data=this.newResourceResponse(attributes);
        if(this instanceof IStaticCacheableResource){
            this.getCachingStrategy().decorateResponse(data,(IStaticCacheableResource)this);
        }
        this.setResponseHeaders(data,attributes);
        if(!data.dataNeedsToBeWritten(attributes)||data.getErrorCode()!=null||!this.needsBody(data.getStatusCode())){
            return;
        }
        if(data.getWriteCallback()==null){
            throw new IllegalStateException("ResourceResponse#setWriteCallback() must be set.");
        }
        data.getWriteCallback().writeData(attributes);
    }
    private boolean needsBody(final Integer statusCode){
        return statusCode==null||(statusCode<300&&statusCode!=204&&statusCode!=205);
    }
    private void checkHeaderAccess(String name){
        name=(String)Args.notEmpty((CharSequence)name.trim().toLowerCase(),"name");
        if(AbstractResource.INTERNAL_HEADERS.contains(name)){
            throw new IllegalArgumentException("you are not allowed to directly access header ["+name+"], "+"use one of the other specialized methods of "+this.getClass().getSimpleName()+" to get or modify its value");
        }
    }
    protected void setResponseHeaders(final ResourceResponse data,final Attributes attributes){
        final Response response=attributes.getResponse();
        if(response instanceof WebResponse){
            final WebResponse webResponse=(WebResponse)response;
            final Time lastModified=data.getLastModified();
            if(lastModified!=null){
                webResponse.setLastModifiedTime(lastModified);
            }
            this.configureCache(data,attributes);
            if(!data.dataNeedsToBeWritten(attributes)){
                webResponse.setStatus(304);
                return;
            }
            if(data.getErrorCode()!=null){
                webResponse.sendError((int)data.getErrorCode(),data.getErrorMessage());
                return;
            }
            if(data.getStatusCode()!=null){
                webResponse.setStatus((int)data.getStatusCode());
                return;
            }
            final String fileName=data.getFileName();
            final ContentDisposition disposition=data.getContentDisposition();
            final String mimeType=data.getContentType();
            final long contentLength=data.getContentLength();
            if(ContentDisposition.ATTACHMENT==disposition){
                webResponse.setAttachmentHeader(fileName);
            }
            else if(ContentDisposition.INLINE==disposition){
                webResponse.setInlineHeader(fileName);
            }
            if(mimeType!=null){
                final String encoding=data.getTextEncoding();
                if(encoding==null){
                    webResponse.setContentType(mimeType);
                }
                else{
                    webResponse.setContentType(mimeType+"; charset="+encoding);
                }
            }
            if(contentLength!=-1L){
                webResponse.setContentLength(contentLength);
            }
            final HttpHeaderCollection headers=data.getHeaders();
            for(final String name : headers.getHeaderNames()){
                this.checkHeaderAccess(name);
                for(final String value : headers.getHeaderValues(name)){
                    webResponse.addHeader(name,value);
                }
            }
            this.flushResponseAfterHeaders(webResponse);
        }
    }
    protected void flushResponseAfterHeaders(final WebResponse response){
        response.flush();
    }
    static{
        (INTERNAL_HEADERS=new HashSet()).add("server");
        AbstractResource.INTERNAL_HEADERS.add("date");
        AbstractResource.INTERNAL_HEADERS.add("expires");
        AbstractResource.INTERNAL_HEADERS.add("last-modified");
        AbstractResource.INTERNAL_HEADERS.add("content-type");
        AbstractResource.INTERNAL_HEADERS.add("content-length");
        AbstractResource.INTERNAL_HEADERS.add("content-disposition");
        AbstractResource.INTERNAL_HEADERS.add("transfer-encoding");
        AbstractResource.INTERNAL_HEADERS.add("connection");
        AbstractResource.INTERNAL_HEADERS.add("content-disposition");
    }
    public static class ResourceResponse{
        private Integer errorCode;
        private Integer statusCode;
        private String errorMessage;
        private String fileName;
        private ContentDisposition contentDisposition;
        private String contentType;
        private String textEncoding;
        private long contentLength;
        private Time lastModified;
        private WriteCallback writeCallback;
        private Duration cacheDuration;
        private WebResponse.CacheScope cacheScope;
        private final HttpHeaderCollection headers;
        public ResourceResponse(){
            super();
            this.fileName=null;
            this.contentDisposition=ContentDisposition.INLINE;
            this.contentType=null;
            this.contentLength=-1L;
            this.lastModified=null;
            this.cacheScope=WebResponse.CacheScope.PRIVATE;
            this.headers=new HttpHeaderCollection();
        }
        public void setError(final Integer errorCode){
            this.setError(errorCode,null);
        }
        public void setError(final Integer errorCode,final String errorMessage){
            this.errorCode=errorCode;
            this.errorMessage=errorMessage;
        }
        public Integer getErrorCode(){
            return this.errorCode;
        }
        public void setStatusCode(final Integer statusCode){
            this.statusCode=statusCode;
        }
        public Integer getStatusCode(){
            return this.statusCode;
        }
        public String getErrorMessage(){
            return this.errorMessage;
        }
        public void setFileName(final String fileName){
            this.fileName=fileName;
        }
        public String getFileName(){
            return this.fileName;
        }
        public void setContentDisposition(final ContentDisposition contentDisposition){
            Args.notNull((Object)contentDisposition,"contentDisposition");
            this.contentDisposition=contentDisposition;
        }
        public ContentDisposition getContentDisposition(){
            return this.contentDisposition;
        }
        public void setContentType(final String contentType){
            this.contentType=contentType;
        }
        public String getContentType(){
            if(this.contentType==null&&this.fileName!=null){
                this.contentType=Application.get().getMimeType(this.fileName);
            }
            return this.contentType;
        }
        public void setTextEncoding(final String textEncoding){
            this.textEncoding=textEncoding;
        }
        protected String getTextEncoding(){
            return this.textEncoding;
        }
        public void setContentLength(final long contentLength){
            this.contentLength=contentLength;
        }
        public long getContentLength(){
            return this.contentLength;
        }
        public void setLastModified(final Time lastModified){
            this.lastModified=lastModified;
        }
        public Time getLastModified(){
            return this.lastModified;
        }
        public boolean dataNeedsToBeWritten(final Attributes attributes){
            final WebRequest request=(WebRequest)attributes.getRequest();
            final Time ifModifiedSince=request.getIfModifiedSinceHeader();
            if(this.cacheDuration!=Duration.NONE&&ifModifiedSince!=null&&this.lastModified!=null){
                final Time roundedLastModified=Time.millis(this.lastModified.getMilliseconds()/1000L*1000L);
                return ifModifiedSince.before((AbstractTimeValue)roundedLastModified);
            }
            return true;
        }
        public void disableCaching(){
            this.setCacheDuration(Duration.NONE);
        }
        public void setCacheDurationToMaximum(){
            this.cacheDuration=WebResponse.MAX_CACHE_DURATION;
        }
        public void setCacheDuration(final Duration duration){
            this.cacheDuration=(Duration)Args.notNull((Object)duration,"duration");
        }
        public Duration getCacheDuration(){
            Duration duration=this.cacheDuration;
            if(duration==null&&Application.exists()){
                duration=Application.get().getResourceSettings().getDefaultCacheDuration();
            }
            return duration;
        }
        public WebResponse.CacheScope getCacheScope(){
            return this.cacheScope;
        }
        public void setCacheScope(final WebResponse.CacheScope scope){
            this.cacheScope=(WebResponse.CacheScope)Args.notNull((Object)scope,"scope");
        }
        public void setWriteCallback(final WriteCallback writeCallback){
            Args.notNull((Object)writeCallback,"writeCallback");
            this.writeCallback=writeCallback;
        }
        public WriteCallback getWriteCallback(){
            return this.writeCallback;
        }
        public HttpHeaderCollection getHeaders(){
            return this.headers;
        }
    }
    public abstract static class WriteCallback{
        public abstract void writeData(final Attributes p0);
        protected final void writeStream(final Attributes attributes,final InputStream stream){
            final Response response=attributes.getResponse();
            final OutputStream s=new OutputStream(){
                public void write(final int b) throws IOException{
                    response.write(new byte[] { (byte)b });
                }
                public void write(final byte[] b) throws IOException{
                    response.write(b);
                }
                public void write(final byte[] b,final int off,final int len) throws IOException{
                    response.write(b,off,len);
                }
            };
            try{
                Streams.copy(stream,s);
            }
            catch(IOException e){
                throw new WicketRuntimeException(e);
            }
        }
    }
}
