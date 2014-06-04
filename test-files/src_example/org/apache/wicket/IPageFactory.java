package org.apache.wicket;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;

public interface IPageFactory{
     <C extends IRequestablePage> IRequestablePage newPage(Class<C> p0);
     <C extends IRequestablePage> IRequestablePage newPage(Class<C> p0,PageParameters p1);
     <C extends IRequestablePage> boolean isBookmarkable(Class<C> p0);
}
