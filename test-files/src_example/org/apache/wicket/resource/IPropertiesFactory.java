package org.apache.wicket.resource;

public interface IPropertiesFactory{
    void addListener(IPropertiesChangeListener p0);
    void clearCache();
    Properties load(Class<?> p0,String p1);
}
