package org.apache.wicket.settings;

import org.apache.wicket.*;

public interface ISessionSettings{
    @Deprecated
    IPageFactory getPageFactory();
    @Deprecated
    void setPageFactory(IPageFactory p0);
}
