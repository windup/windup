package org.apache.wicket.page;

import java.io.*;

public interface IPageManagerContext{
    void setRequestData(Object p0);
    Object getRequestData();
    void setSessionAttribute(String p0,Serializable p1);
    Serializable getSessionAttribute(String p0);
    void bind();
    String getSessionId();
}
