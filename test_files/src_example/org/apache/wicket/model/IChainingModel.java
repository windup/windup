package org.apache.wicket.model;

public interface IChainingModel<T> extends IModel<T>{
    void setChainedModel(IModel<?> p0);
    IModel<?> getChainedModel();
}
