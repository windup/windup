package org.apache.wicket.request.component;

import org.apache.wicket.page.*;
import org.apache.wicket.request.mapper.parameter.*;

public interface IRequestablePage extends IRequestableComponent,IManageablePage{
    void renderPage();
    boolean isBookmarkable();
    int getRenderCount();
    boolean wasCreatedBookmarkable();
    PageParameters getPageParameters();
}
