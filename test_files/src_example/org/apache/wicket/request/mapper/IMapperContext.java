package org.apache.wicket.request.mapper;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.request.component.*;

public interface IMapperContext extends IPageSource{
    String getNamespace();
    String getPageIdentifier();
    String getBookmarkableIdentifier();
    String getResourceIdentifier();
    ResourceReferenceRegistry getResourceReferenceRegistry();
    String requestListenerInterfaceToString(RequestListenerInterface p0);
    RequestListenerInterface requestListenerInterfaceFromString(String p0);
    Class<? extends IRequestablePage> getHomePageClass();
}
