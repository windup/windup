package org.apache.wicket.request.handler;

import org.apache.wicket.request.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;

public interface IPageClassRequestHandler extends IRequestHandler{
    Class<? extends IRequestablePage> getPageClass();
    PageParameters getPageParameters();
}
