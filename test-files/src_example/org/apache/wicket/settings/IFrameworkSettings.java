package org.apache.wicket.settings;

import org.apache.wicket.*;
import org.apache.wicket.serialize.*;

public interface IFrameworkSettings extends IEventDispatcher{
    String getVersion();
    IDetachListener getDetachListener();
    void setDetachListener(IDetachListener p0);
    void add(IEventDispatcher p0);
    void setSerializer(ISerializer p0);
    ISerializer getSerializer();
}
