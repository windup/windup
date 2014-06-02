package org.apache.wicket.model;

public interface IWrapModel<T> extends IModel<T>{
    IModel<?> getWrappedModel();
}
