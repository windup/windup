package org.apache.wicket.resource.aggregation;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.resource.dependencies.*;
import org.apache.wicket.request.mapper.parameter.*;
import java.util.*;
import org.apache.wicket.resource.*;

public abstract class AbstractResourceAggregatingHeaderResponse<R extends ResourceReferenceCollection,K> extends DecoratingHeaderResponse{
    private final List<ResourceReferenceAndStringData> topLevelReferences;
    public AbstractResourceAggregatingHeaderResponse(final IHeaderResponse real){
        super(real);
        this.topLevelReferences=(List<ResourceReferenceAndStringData>)new ArrayList();
    }
    public void renderJavaScriptReference(final ResourceReference reference){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,null,null,null,AbstractResourceDependentResourceReference.ResourceType.JS,false,null,null));
    }
    public void renderJavaScriptReference(final ResourceReference reference,final String id){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,null,null,id,AbstractResourceDependentResourceReference.ResourceType.JS,false,null,null));
    }
    public void renderCSSReference(final ResourceReference reference){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,null,null,null,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,null));
    }
    public void renderCSSReference(final ResourceReference reference,final String media){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,null,null,media,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,null));
    }
    public void close(){
        final SortedMap<K,R> map=(SortedMap<K,R>)new TreeMap(this.getGroupingKeyComparator());
        for(final ResourceReferenceAndStringData ref : this.topLevelReferences){
            final K key=this.newGroupingKey(ref);
            R coll=(R)map.get(key);
            if(coll==null){
                map.put(key,coll=this.newResourceReferenceCollection(key));
            }
            coll.add(ref);
        }
        final Set<ResourceReferenceAndStringData> alreadyRendered=(Set<ResourceReferenceAndStringData>)new LinkedHashSet();
        for(final Map.Entry<K,R> entry : map.entrySet()){
            this.renderCollection(alreadyRendered,entry.getKey(),(ResourceReferenceCollection)entry.getValue());
        }
        this.onAllCollectionsRendered(this.topLevelReferences);
        super.close();
    }
    protected R newResourceReferenceCollection(final K key){
        return (R)new ResourceReferenceCollection();
    }
    protected abstract K newGroupingKey(final ResourceReferenceAndStringData p0);
    protected Comparator<K> getGroupingKeyComparator(){
        return null;
    }
    protected void renderCollection(final Set<ResourceReferenceAndStringData> alreadyRendered,final K key,final R coll){
        for(final ResourceReferenceAndStringData data : coll){
            this.renderIfNotAlreadyRendered(alreadyRendered,data);
        }
    }
    protected void renderIfNotAlreadyRendered(final Set<ResourceReferenceAndStringData> alreadyRendered,final ResourceReferenceAndStringData data){
        if(!alreadyRendered.contains(data)){
            this.render(data);
            alreadyRendered.add(data);
        }
    }
    protected void render(final ResourceReferenceAndStringData data){
        ResourceUtil.renderTo(this.getRealResponse(),data);
    }
    protected void onAllCollectionsRendered(final List<ResourceReferenceAndStringData> allTopLevelReferences){
    }
    public void renderJavaScriptReference(final String url){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,null,AbstractResourceDependentResourceReference.ResourceType.JS,false,null,null));
    }
    public void renderJavaScriptReference(final String url,final String id){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,id,AbstractResourceDependentResourceReference.ResourceType.JS,false,null,null));
    }
    public void renderCSSReference(final String url){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,null,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,null));
    }
    public void renderCSSReference(final String url,final String media){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,media,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,null));
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters parameters,final String id){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,parameters,null,id,AbstractResourceDependentResourceReference.ResourceType.JS,false,null,null));
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters parameters,final String id,final boolean defer){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,parameters,null,id,AbstractResourceDependentResourceReference.ResourceType.JS,defer,null,null));
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters parameters,final String id,final boolean defer,final String charset){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,parameters,null,id,AbstractResourceDependentResourceReference.ResourceType.JS,defer,charset,null));
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,id,AbstractResourceDependentResourceReference.ResourceType.JS,defer,null,null));
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer,final String charset){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,id,AbstractResourceDependentResourceReference.ResourceType.JS,defer,charset,null));
    }
    public void renderJavaScript(final CharSequence javascript,final String id){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(javascript,AbstractResourceDependentResourceReference.ResourceType.JS,id));
    }
    public void renderCSS(final CharSequence css,final String media){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(css,AbstractResourceDependentResourceReference.ResourceType.CSS,media));
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,pageParameters,null,media,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,null));
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media,final String condition){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(reference,pageParameters,null,media,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,condition));
    }
    public void renderCSSReference(final String url,final String media,final String condition){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(null,null,url,media,AbstractResourceDependentResourceReference.ResourceType.CSS,false,null,condition));
    }
    public void renderString(final CharSequence string){
        this.topLevelReferences.add(new ResourceReferenceAndStringData(string,AbstractResourceDependentResourceReference.ResourceType.PLAIN,null));
    }
    public void renderOnDomReadyJavaScript(final String javascript){
        super.renderOnDomReadyJavaScript(javascript);
    }
    public void renderOnLoadJavaScript(final String javascript){
        super.renderOnLoadJavaScript(javascript);
    }
    public void renderOnEventJavaScript(final String target,final String event,final String javascript){
        super.renderOnEventJavaScript(target,event,javascript);
    }
}
