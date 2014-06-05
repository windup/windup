package org.apache.wicket.resource.aggregation;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.resource.dependencies.*;
import java.util.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;

public abstract class AbstractDependencyRespectingResourceAggregatingHeaderResponse<R extends ResourceReferenceCollection,K> extends AbstractResourceAggregatingHeaderResponse<R,K>{
    public AbstractDependencyRespectingResourceAggregatingHeaderResponse(final IHeaderResponse real){
        super(real);
    }
    protected void renderCollection(final Set<ResourceReferenceAndStringData> alreadyRendered,final K key,final R coll){
        for(final ResourceReferenceAndStringData data : coll){
            final ResourceReference ref=data.getReference();
            if(ref instanceof AbstractResourceDependentResourceReference){
                final AbstractResourceDependentResourceReference parent=(AbstractResourceDependentResourceReference)ref;
                final R childColl=this.newResourceReferenceCollection(key);
                for(final AbstractResourceDependentResourceReference child : parent.getDependentResourceReferences()){
                    childColl.add(toData(child));
                }
                this.renderCollection(alreadyRendered,key,childColl);
            }
            this.renderIfNotAlreadyRendered(alreadyRendered,data);
        }
    }
    private static ResourceReferenceAndStringData toData(final AbstractResourceDependentResourceReference reference){
        final boolean css=AbstractResourceDependentResourceReference.ResourceType.CSS.equals(reference.getResourceType());
        final String string=css?reference.getMedia():reference.getUniqueId();
        return new ResourceReferenceAndStringData(reference,null,null,string,reference.getResourceType(),false,null,null);
    }
}
