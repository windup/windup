package org.apache.wicket.model;

public abstract class AbstractReadOnlyModel<T> implements IModel<T>{
    private static final long serialVersionUID=1L;
    public abstract T getObject();
    public final void setObject(final T object){
        throw new UnsupportedOperationException("Model "+this.getClass()+" does not support setObject(Object)");
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("Model:classname=[");
        sb.append(this.getClass().getName()).append("]");
        return sb.toString();
    }
    public void detach(){
    }
}
