package org.apache.wicket.request.handler;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;

public interface IPageProvider{
    IRequestablePage getPageInstance();
    PageParameters getPageParameters();
    boolean isNewPageInstance();
    Class<? extends IRequestablePage> getPageClass();
    Integer getPageId();
    Integer getRenderCount();
    void detach();
}
