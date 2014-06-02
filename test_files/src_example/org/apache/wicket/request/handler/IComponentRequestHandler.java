package org.apache.wicket.request.handler;

import org.apache.wicket.request.*;
import org.apache.wicket.request.component.*;

public interface IComponentRequestHandler extends IRequestHandler{
    IRequestableComponent getComponent();
    String getComponentPath();
}
