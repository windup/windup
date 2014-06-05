package org.apache.wicket.model;

import org.apache.wicket.*;

public class ComponentDetachableModel<T> implements IModel<T>,IComponentAssignedModel<T>{
    private static final long serialVersionUID=1L;
    private transient boolean attached;
    public ComponentDetachableModel(){
        super();
        this.attached=false;
    }
    public final T getObject(){
        throw new RuntimeException("get object call not expected on a IComponentAssignedModel");
    }
    public final void setObject(final T object){
        throw new RuntimeException("set object call not expected on a IComponentAssignedModel");
    }
    public final boolean isAttached(){
        return this.attached;
    }
    protected final void setAttached(){
        this.attached=true;
    }
    public void detach(){
    }
    protected void attach(){
    }
    protected T getObject(final Component component){
        return null;
    }
    protected void setObject(final Component component,final T object){
    }
    public IWrapModel<T> wrapOnAssignment(final Component comp){
        return new WrapModel<Object>(comp);
    }
    private class WrapModel<P> implements IWrapModel<T>{
        private static final long serialVersionUID=1L;
        private final Component component;
        public WrapModel(final Component comp){
            super();
            this.component=comp;
        }
        public IModel<T> getWrappedModel(){
            return (IModel<T>)ComponentDetachableModel.this;
        }
        private void attach(){
            if(!ComponentDetachableModel.this.attached){
                ComponentDetachableModel.this.attached=true;
                ComponentDetachableModel.this.attach();
            }
        }
        public T getObject(){
            this.attach();
            return ComponentDetachableModel.this.getObject(this.component);
        }
        public void setObject(final T object){
            this.attach();
            ComponentDetachableModel.this.setObject(this.component,object);
        }
        public void detach(){
            if(ComponentDetachableModel.this.attached){
                ComponentDetachableModel.this.attached=false;
                ComponentDetachableModel.this.detach();
            }
        }
    }
}
