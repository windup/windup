package org.apache.wicket.session;

import org.apache.wicket.request.*;
import java.io.*;
import org.apache.wicket.*;
import java.util.*;

public interface ISessionStore{
    Serializable getAttribute(Request p0,String p1);
    List<String> getAttributeNames(Request p0);
    void setAttribute(Request p0,String p1,Serializable p2);
    void removeAttribute(Request p0,String p1);
    void invalidate(Request p0);
    String getSessionId(Request p0,boolean p1);
    Session lookup(Request p0);
    void bind(Request p0,Session p1);
    void flushSession(Request p0,Session p1);
    void destroy();
    void registerUnboundListener(UnboundListener p0);
    void unregisterUnboundListener(UnboundListener p0);
    Set<UnboundListener> getUnboundListener();
    public interface BindListener{
        void bindingSession(Request p0,Session p1);
    }
    public interface UnboundListener{
        void sessionUnbound(String p0);
    }
}
