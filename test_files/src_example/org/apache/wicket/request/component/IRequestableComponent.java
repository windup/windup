package org.apache.wicket.request.component;

import org.apache.wicket.behavior.*;

public interface IRequestableComponent{
    String getPageRelativePath();
    String getId();
    IRequestablePage getPage();
    IRequestableComponent get(String p0);
    boolean canCallListenerInterface();
    int getBehaviorId(Behavior p0);
    Behavior getBehaviorById(int p0);
    void detach();
}
