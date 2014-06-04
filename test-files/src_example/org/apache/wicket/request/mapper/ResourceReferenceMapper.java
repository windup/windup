package org.apache.wicket.request.mapper;

import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.request.*;

public class ResourceReferenceMapper extends ParentPathReferenceRewriter{
    public ResourceReferenceMapper(final IPageParametersEncoder pageParametersEncoder,final IProvider<String> parentPathPartEscapeSequence,final IProvider<IResourceCachingStrategy> cachingStrategy){
        super((IRequestMapper)new BasicResourceReferenceMapper(pageParametersEncoder,cachingStrategy),(IProvider)parentPathPartEscapeSequence);
    }
}
