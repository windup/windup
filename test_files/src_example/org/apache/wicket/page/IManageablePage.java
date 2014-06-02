package org.apache.wicket.page;

import org.apache.wicket.*;

public interface IManageablePage extends IClusterable{
    boolean isPageStateless();
    int getPageId();
    void detach();
    boolean setFreezePageId(boolean p0);
}
