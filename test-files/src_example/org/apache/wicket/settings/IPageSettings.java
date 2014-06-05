package org.apache.wicket.settings;

import org.apache.wicket.markup.resolver.*;
import java.util.*;

public interface IPageSettings{
    void addComponentResolver(IComponentResolver p0);
    List<IComponentResolver> getComponentResolvers();
    boolean getVersionPagesByDefault();
    void setVersionPagesByDefault(boolean p0);
    boolean getRecreateMountedPagesAfterExpiry();
    void setRecreateMountedPagesAfterExpiry(boolean p0);
}
