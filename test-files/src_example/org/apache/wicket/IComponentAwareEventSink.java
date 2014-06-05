package org.apache.wicket;

import org.apache.wicket.event.*;

public interface IComponentAwareEventSink{
    void onEvent(Component p0,IEvent<?> p1);
}
