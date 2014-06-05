package org.apache.wicket.request.resource;

import org.apache.wicket.request.resource.caching.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.markup.html.*;
import org.slf4j.*;
import org.apache.wicket.*;

public class PackageResource extends AbstractResource implements IStaticCacheableResource{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    private final String absolutePath;
    private final Locale locale;
    private final String path;
    private final String scopeName;
    private final String style;
    private final String variation;
    private String textEncoding;
    protected PackageResource(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super();
        this.textEncoding=null;
        this.absolutePath=Packages.absolutePath((Class)scope,name);
        final String parentEscape=this.getParentFolderPlaceholder();
        if(!Strings.isEmpty((CharSequence)parentEscape)){
            this.path=Strings.replaceAll((CharSequence)name,(CharSequence)"../",(CharSequence)(parentEscape+"/")).toString();
        }
        else{
            this.path=name;
        }
        this.scopeName=scope.getName();
        this.locale=locale;
        this.style=style;
        this.variation=variation;
    }
    private Locale getCurrentLocale(){
        return (this.locale!=null)?this.locale:Session.get().getLocale();
    }
    private String getCurrentStyle(){
        return (this.style!=null)?this.style:Session.get().getStyle();
    }
    public String getTextEncoding(){
        return this.textEncoding;
    }
    public void setTextEncoding(final String textEncoding){
        this.textEncoding=textEncoding;
    }
    public Serializable getCacheKey(){
        final IResourceStream stream=this.getCacheableResourceStream();
        if(stream==null){
            return null;
        }
        return new CacheKey(this.scopeName,this.absolutePath,stream.getLocale(),stream.getStyle(),stream.getVariation());
    }
    public final Class<?> getScope(){
        return WicketObjects.resolveClass(this.scopeName);
    }
    public final String getStyle(){
        return this.style;
    }
    protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
        final ResourceResponse resourceResponse=new ResourceResponse();
        final IResourceStream resourceStream=this.getResourceStream();
        if(resourceStream==null){
            return this.sendResourceError(resourceResponse,404,"Unable to find resource");
        }
        final Time lastModified=resourceStream.lastModifiedTime();
        resourceResponse.setLastModified(lastModified);
        if(resourceResponse.dataNeedsToBeWritten(attributes)){
            String contentType=resourceStream.getContentType();
            if(contentType==null&&Application.exists()){
                contentType=Application.get().getMimeType(this.path);
            }
            resourceResponse.setContentType(contentType);
            resourceResponse.setTextEncoding(this.getTextEncoding());
            try{
                final byte[] bytes=IOUtils.toByteArray(resourceStream.getInputStream());
                final byte[] processed=this.processResponse(attributes,bytes);
                resourceResponse.setContentLength(processed.length);
                resourceResponse.setWriteCallback(new WriteCallback(){
                    public void writeData(final IResource.Attributes attributes){
                        attributes.getResponse().write(processed);
                    }
                });
            }
            catch(IOException e){
                PackageResource.log.debug(e.getMessage(),e);
                return this.sendResourceError(resourceResponse,500,"Unable to read resource stream");
            }
            catch(ResourceStreamNotFoundException e2){
                PackageResource.log.debug(e2.getMessage(),(Throwable)e2);
                return this.sendResourceError(resourceResponse,500,"Unable to open resource stream");
            }
            finally{
                try{
                    resourceStream.close();
                }
                catch(IOException e3){
                    PackageResource.log.warn("Unable to close the resource stream",e3);
                }
            }
        }
        return resourceResponse;
    }
    protected byte[] processResponse(final IResource.Attributes attributes,final byte[] original){
        return original;
    }
    private ResourceResponse sendResourceError(final ResourceResponse resourceResponse,final int errorCode,final String errorMessage){
        final String msg=String.format("resource [path = %s, style = %s, variation = %s, locale = %s]: %s (status=%d)",new Object[] { this.absolutePath,this.style,this.variation,this.locale,errorMessage,errorCode });
        PackageResource.log.warn(msg);
        resourceResponse.setError(errorCode,errorMessage);
        return resourceResponse;
    }
    public IResourceStream getCacheableResourceStream(){
        return this.internalGetResourceStream(this.getCurrentStyle(),this.getCurrentLocale());
    }
    protected IResourceStream getResourceStream(){
        return this.internalGetResourceStream(this.style,this.locale);
    }
    private IResourceStream internalGetResourceStream(final String style,final Locale locale){
        final IResourceStreamLocator resourceStreamLocator=Application.get().getResourceSettings().getResourceStreamLocator();
        final IResourceStream resourceStream=resourceStreamLocator.locate(this.getScope(),this.absolutePath,style,this.variation,locale,null,false);
        Class<?> realScope=this.getScope();
        String realPath=this.absolutePath;
        if(resourceStream instanceof IFixedLocationResourceStream){
            realPath=((IFixedLocationResourceStream)resourceStream).locationAsString();
            if(realPath!=null){
                final int index=realPath.indexOf(this.absolutePath);
                if(index!=-1){
                    realPath=realPath.substring(index);
                }
                else{
                    realScope=null;
                }
            }
            else{
                realPath=this.absolutePath;
            }
        }
        if(!this.accept(realScope,realPath)){
            throw new PackageResourceBlockedException("Access denied to (static) package resource "+this.absolutePath+". See IPackageResourceGuard");
        }
        return resourceStream;
    }
    private boolean accept(final Class<?> scope,final String path){
        final IPackageResourceGuard guard=Application.get().getResourceSettings().getPackageResourceGuard();
        return guard.accept(scope,path);
    }
    public static boolean exists(final Class<?> scope,final String path,final Locale locale,final String style,final String variation){
        final String absolutePath=Packages.absolutePath((Class)scope,path);
        return Application.get().getResourceSettings().getResourceStreamLocator().locate(scope,absolutePath,style,variation,locale,null,false)!=null;
    }
    public String toString(){
        final StringBuilder result=new StringBuilder();
        result.append('[').append(this.getClass().getSimpleName()).append(' ').append("name = ").append(this.path).append(", scope = ").append(this.scopeName).append(", locale = ").append(this.locale).append(", style = ").append(this.style).append(", variation = ").append(this.variation).append(']');
        return result.toString();
    }
    public int hashCode(){
        final int prime=31;
        int result=1;
        result=31*result+((this.absolutePath==null)?0:this.absolutePath.hashCode());
        result=31*result+((this.locale==null)?0:this.locale.hashCode());
        result=31*result+((this.path==null)?0:this.path.hashCode());
        result=31*result+((this.scopeName==null)?0:this.scopeName.hashCode());
        result=31*result+((this.style==null)?0:this.style.hashCode());
        result=31*result+((this.variation==null)?0:this.variation.hashCode());
        return result;
    }
    public boolean equals(final Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(this.getClass()!=obj.getClass()){
            return false;
        }
        final PackageResource other=(PackageResource)obj;
        if(this.absolutePath==null){
            if(other.absolutePath!=null){
                return false;
            }
        }
        else if(!this.absolutePath.equals(other.absolutePath)){
            return false;
        }
        if(this.locale==null){
            if(other.locale!=null){
                return false;
            }
        }
        else if(!this.locale.equals(other.locale)){
            return false;
        }
        if(this.path==null){
            if(other.path!=null){
                return false;
            }
        }
        else if(!this.path.equals(other.path)){
            return false;
        }
        if(this.scopeName==null){
            if(other.scopeName!=null){
                return false;
            }
        }
        else if(!this.scopeName.equals(other.scopeName)){
            return false;
        }
        if(this.style==null){
            if(other.style!=null){
                return false;
            }
        }
        else if(!this.style.equals(other.style)){
            return false;
        }
        if(this.variation==null){
            if(other.variation!=null){
                return false;
            }
        }
        else if(!this.variation.equals(other.variation)){
            return false;
        }
        return true;
    }
    String getParentFolderPlaceholder(){
        String parentFolderPlaceholder;
        if(Application.exists()){
            parentFolderPlaceholder=Application.get().getResourceSettings().getParentFolderPlaceholder();
        }
        else{
            parentFolderPlaceholder="..";
        }
        return parentFolderPlaceholder;
    }
    static{
        log=LoggerFactory.getLogger(PackageResource.class);
    }
    public static final class PackageResourceBlockedException extends WicketRuntimeException{
        private static final long serialVersionUID=1L;
        public PackageResourceBlockedException(final String message){
            super(message);
        }
    }
    private static class CacheKey implements Serializable{
        private final String scopeName;
        private final String path;
        private final Locale locale;
        private final String style;
        private final String variation;
        public CacheKey(final String scopeName,final String path,final Locale locale,final String style,final String variation){
            super();
            this.scopeName=scopeName;
            this.path=path;
            this.locale=locale;
            this.style=style;
            this.variation=variation;
        }
        public boolean equals(final Object o){
            if(this==o){
                return true;
            }
            if(!(o instanceof CacheKey)){
                return false;
            }
            final CacheKey cacheKey=(CacheKey)o;
            Label_0054:{
                if(this.locale!=null){
                    if(this.locale.equals(cacheKey.locale)){
                        break Label_0054;
                    }
                }
                else if(cacheKey.locale==null){
                    break Label_0054;
                }
                return false;
            }
            if(!this.path.equals(cacheKey.path)){
                return false;
            }
            if(!this.scopeName.equals(cacheKey.scopeName)){
                return false;
            }
            Label_0119:{
                if(this.style!=null){
                    if(this.style.equals(cacheKey.style)){
                        break Label_0119;
                    }
                }
                else if(cacheKey.style==null){
                    break Label_0119;
                }
                return false;
            }
            if(this.variation!=null){
                if(this.variation.equals(cacheKey.variation)){
                    return true;
                }
            }
            else if(cacheKey.variation==null){
                return true;
            }
            return false;
        }
        public int hashCode(){
            int result=this.scopeName.hashCode();
            result=31*result+this.path.hashCode();
            result=31*result+((this.locale!=null)?this.locale.hashCode():0);
            result=31*result+((this.style!=null)?this.style.hashCode():0);
            result=31*result+((this.variation!=null)?this.variation.hashCode():0);
            return result;
        }
        public String toString(){
            final StringBuilder sb=new StringBuilder();
            sb.append("CacheKey");
            sb.append("{scopeName='").append(this.scopeName).append('\'');
            sb.append(", path='").append(this.path).append('\'');
            sb.append(", locale=").append(this.locale);
            sb.append(", style='").append(this.style).append('\'');
            sb.append(", variation='").append(this.variation).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
