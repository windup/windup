package org.apache.wicket.settings;

import org.apache.wicket.*;
import org.apache.wicket.application.*;
import org.apache.wicket.util.lang.*;

public interface IApplicationSettings{
    Class<? extends Page> getAccessDeniedPage();
    IClassResolver getClassResolver();
    Bytes getDefaultMaximumUploadSize();
    Class<? extends Page> getInternalErrorPage();
    Class<? extends Page> getPageExpiredErrorPage();
    boolean isUploadProgressUpdatesEnabled();
    void setAccessDeniedPage(Class<? extends Page> p0);
    void setClassResolver(IClassResolver p0);
    void setDefaultMaximumUploadSize(Bytes p0);
    void setInternalErrorPage(Class<? extends Page> p0);
    void setPageExpiredErrorPage(Class<? extends Page> p0);
    void setUploadProgressUpdatesEnabled(boolean p0);
}
