package org.apache.wicket.model;

import org.apache.wicket.*;

public class ComponentModel<T> implements IModel<T>,IComponentAssignedModel<T>{
    private static final long serialVersionUID=1L;
    public final T getObject(){
        throw new RuntimeException("get object call not expected on a IComponentAssignedModel");
    }
    public final void setObject(final Object object){
        throw new RuntimeException("set object call not expected on a IComponentAssignedModel");
    }
    protected T getObject(final Component component){
        return null;
    }
    protected void setObject(final Component component,final Object object){
    }
    public void detach(){
    }
    public IWrapModel<T> wrapOnAssignment(final Component comp){
        return new WrapModel(comp);
    }
    private class WrapModel implements IWrapModel<T>{
        private static final long serialVersionUID=1L;
        private final Component component;
        public WrapModel(final Component comp){
            super();
            this.component=comp;
        }
        public IModel<T> getWrappedModel(){
            return (IModel<T>)ComponentModel.this;
        }
        public T getObject(){
            return ComponentModel.this.getObject(this.component);
        }
        public void setObject(final Object object){
            ComponentModel.this.setObject(this.component,object);
        }
        public void detach(){
            ComponentModel.this.detach();
        }
    }
}
