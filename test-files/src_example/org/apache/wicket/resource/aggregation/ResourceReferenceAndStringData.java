package org.apache.wicket.resource.aggregation;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.resource.dependencies.*;

public class ResourceReferenceAndStringData{
    private final ResourceReference reference;
    private final PageParameters parameters;
    private final String url;
    private final String idOrMedia;
    private final AbstractResourceDependentResourceReference.ResourceType type;
    private final boolean jsDefer;
    private final String charset;
    private final String cssCondition;
    private final CharSequence content;
    public ResourceReferenceAndStringData(final ResourceReference reference,final PageParameters parameters,final String url,final String idOrMedia,final AbstractResourceDependentResourceReference.ResourceType type,final boolean jsDefer,final String charset,final String cssCondition){
        super();
        this.reference=reference;
        this.parameters=parameters;
        this.url=url;
        this.idOrMedia=idOrMedia;
        this.type=type;
        this.jsDefer=jsDefer;
        this.charset=charset;
        this.cssCondition=cssCondition;
        this.content=null;
    }
    public ResourceReferenceAndStringData(final ResourceReference reference,final String idOrMedia,final boolean isCss){
        super();
        this.reference=reference;
        this.parameters=null;
        this.url=null;
        this.idOrMedia=idOrMedia;
        this.type=(isCss?AbstractResourceDependentResourceReference.ResourceType.CSS:AbstractResourceDependentResourceReference.ResourceType.JS);
        this.jsDefer=false;
        this.charset=null;
        this.cssCondition=null;
        this.content=null;
    }
    public ResourceReferenceAndStringData(final CharSequence content,final AbstractResourceDependentResourceReference.ResourceType type,final String idOrMedia){
        super();
        this.content=content;
        this.type=type;
        this.reference=null;
        this.parameters=null;
        this.url=null;
        this.idOrMedia=idOrMedia;
        this.jsDefer=false;
        this.charset=null;
        this.cssCondition=null;
    }
    public ResourceReference getReference(){
        return this.reference;
    }
    public PageParameters getParameters(){
        return this.parameters;
    }
    public String getUrl(){
        return this.url;
    }
    public String getIdOrMedia(){
        return this.idOrMedia;
    }
    @Deprecated
    public String getString(){
        return this.getIdOrMedia();
    }
    @Deprecated
    public boolean isCss(){
        return this.type==AbstractResourceDependentResourceReference.ResourceType.CSS;
    }
    public AbstractResourceDependentResourceReference.ResourceType getResourceType(){
        return this.type;
    }
    public boolean isJsDefer(){
        return this.jsDefer;
    }
    public String getCharset(){
        return this.charset;
    }
    public String getCssCondition(){
        return this.cssCondition;
    }
    public CharSequence getContent(){
        return this.content;
    }
    public int hashCode(){
        final int prime=31;
        int result=1;
        result=31*result+((this.charset==null)?0:this.charset.hashCode());
        result=31*result+((this.content==null)?0:this.content.hashCode());
        result=31*result+((this.cssCondition==null)?0:this.cssCondition.hashCode());
        result=31*result+((this.idOrMedia==null)?0:this.idOrMedia.hashCode());
        result=31*result+(this.jsDefer?1231:1237);
        result=31*result+((this.parameters==null)?0:this.parameters.hashCode());
        result=31*result+((this.reference==null)?0:this.reference.hashCode());
        result=31*result+((this.type==null)?0:this.type.hashCode());
        result=31*result+((this.url==null)?0:this.url.hashCode());
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
        final ResourceReferenceAndStringData other=(ResourceReferenceAndStringData)obj;
        if(this.charset==null){
            if(other.charset!=null){
                return false;
            }
        }
        else if(!this.charset.equals(other.charset)){
            return false;
        }
        if(this.content==null){
            if(other.content!=null){
                return false;
            }
        }
        else if(!this.content.equals(other.content)){
            return false;
        }
        if(this.cssCondition==null){
            if(other.cssCondition!=null){
                return false;
            }
        }
        else if(!this.cssCondition.equals(other.cssCondition)){
            return false;
        }
        if(this.idOrMedia==null){
            if(other.idOrMedia!=null){
                return false;
            }
        }
        else if(!this.idOrMedia.equals(other.idOrMedia)){
            return false;
        }
        if(this.jsDefer!=other.jsDefer){
            return false;
        }
        if(this.parameters==null){
            if(other.parameters!=null){
                return false;
            }
        }
        else if(!this.parameters.equals((Object)other.parameters)){
            return false;
        }
        if(this.reference==null){
            if(other.reference!=null){
                return false;
            }
        }
        else if(!this.reference.equals(other.reference)){
            return false;
        }
        if(this.type!=other.type){
            return false;
        }
        if(this.url==null){
            if(other.url!=null){
                return false;
            }
        }
        else if(!this.url.equals(other.url)){
            return false;
        }
        return true;
    }
    public String toString(){
        return "ResourceReferenceAndStringData [reference="+this.reference+", parameters="+this.parameters+", url="+this.url+", idOrMedia="+this.idOrMedia+", type="+this.type+", jsDefer="+this.jsDefer+", charset="+this.charset+", cssCondition="+this.cssCondition+", content="+(Object)this.content+"]";
    }
}
