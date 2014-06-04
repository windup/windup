package org.apache.wicket.pageStore;

import org.apache.wicket.page.*;
import java.io.*;

public interface IPageStore{
    void destroy();
    IManageablePage getPage(String p0,int p1);
    void removePage(String p0,int p1);
    void storePage(String p0,IManageablePage p1);
    void unbind(String p0);
    Serializable prepareForSerialization(String p0,Object p1);
    Object restoreAfterSerialization(Serializable p0);
    IManageablePage convertToPage(Object p0);
}
