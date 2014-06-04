package org.apache.wicket.model;

public abstract class AbstractWrapModel<T> implements IWrapModel<T>{
    private static final long serialVersionUID=1L;
    public T getObject(){
        return null;
    }
    public void setObject(final T object){
    }
    public void detach(){
        this.getWrappedModel().detach();
    }
}
