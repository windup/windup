package org.apache.wicket.model;

public interface IModel<T> extends IDetachable{
    T getObject();
    void setObject(T p0);
}
