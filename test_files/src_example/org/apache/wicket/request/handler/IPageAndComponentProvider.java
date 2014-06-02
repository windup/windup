package org.apache.wicket.request.handler;

import org.apache.wicket.request.component.*;

public interface IPageAndComponentProvider extends IPageProvider,IIntrospectablePageProvider{
    IRequestableComponent getComponent();
    String getComponentPath();
}
