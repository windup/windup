package org.apache.wicket.model;

public interface IObjectClassAwareModel<T> extends IModel<T>{
    Class<T> getObjectClass();
}
