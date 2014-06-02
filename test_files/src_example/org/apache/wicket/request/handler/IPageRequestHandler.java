package org.apache.wicket.request.handler;

import org.apache.wicket.request.component.*;

public interface IPageRequestHandler extends IPageClassRequestHandler{
    IRequestablePage getPage();
    Integer getPageId();
    boolean isPageInstanceCreated();
    Integer getRenderCount();
}
