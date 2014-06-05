package org.apache.wicket.resource.aggregation;

import java.util.*;

public class ResourceReferenceCollection extends LinkedHashSet<ResourceReferenceAndStringData>{
    private static final long serialVersionUID=1L;
    public ResourceReferenceCollection(){
        super();
    }
    public ResourceReferenceCollection(final Collection<? extends ResourceReferenceAndStringData> c){
        super(c);
    }
}
