package org.apache.wicket.page;

public interface IPageManager{
    IPageManagerContext getContext();
    IManageablePage getPage(int p0) throws CouldNotLockPageException;
    void touchPage(IManageablePage p0) throws CouldNotLockPageException;
    boolean supportsVersioning();
    void commitRequest();
    void newSessionCreated();
    void sessionExpired(String p0);
    void destroy();
}
