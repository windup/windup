package weblogic.security.services;

import weblogic.security.services.AppContextElement;

public interface AppContext{
    AppContextElement getElement(String p0);
    AppContextElement[] getElements(String[] p0);
    String[] getNames();
    int size();
}
