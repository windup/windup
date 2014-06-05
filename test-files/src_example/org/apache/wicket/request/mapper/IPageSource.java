package org.apache.wicket.request.mapper;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;

public interface IPageSource{
    IRequestablePage getPageInstance(int p0);
    IRequestablePage newPageInstance(Class<? extends IRequestablePage> p0,PageParameters p1);
}
