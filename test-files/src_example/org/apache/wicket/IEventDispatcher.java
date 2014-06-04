package org.apache.wicket;

import org.apache.wicket.event.*;

public interface IEventDispatcher{
    void dispatchEvent(Object p0,IEvent<?> p1,Component p2);
}
