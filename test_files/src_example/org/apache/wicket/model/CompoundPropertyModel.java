package org.apache.wicket.model;

import org.apache.wicket.*;
import org.apache.wicket.util.string.*;

public class CompoundPropertyModel<T> implements IComponentInheritedModel<T>,IChainingModel<T>{
    private static final long serialVersionUID=1L;
    private Object target;
    public CompoundPropertyModel(final IModel<T> model){
        super();
        this.target=model;
    }
    public CompoundPropertyModel(final T object){
        super();
        this.target=object;
    }
    public T getObject(){
        if(this.target instanceof IModel){
            return ((IModel)this.target).getObject();
        }
        return (T)this.target;
    }
    public void setObject(final T object){
        if(this.target instanceof IModel){
            ((IModel)this.target).setObject(object);
        }
        else{
            this.target=object;
        }
    }
    public IModel<?> getChainedModel(){
        if(this.target instanceof IModel){
            return (IModel<?>)this.target;
        }
        return null;
    }
    public void setChainedModel(final IModel<?> model){
        this.target=model;
    }
    public void detach(){
        if(this.target instanceof IDetachable){
            ((IDetachable)this.target).detach();
        }
    }
    protected String propertyExpression(final Component component){
        return component.getId();
    }
    public <C> IWrapModel<C> wrapOnInheritance(final Component component){
        return new AttachedCompoundPropertyModel<C>(component);
    }
    public <S> IModel<S> bind(final String property){
        return new PropertyModel<S>(this,property);
    }
    public String toString(){
        final AppendingStringBuffer sb=new AppendingStringBuffer().append("Model:classname=["+this.getClass().getName()+"]");
        sb.append(":nestedModel=[").append(this.target).append("]");
        return sb.toString();
    }
    public static <Z> CompoundPropertyModel<Z> of(final IModel<Z> model){
        return new CompoundPropertyModel<Z>(model);
    }
    private class AttachedCompoundPropertyModel<C> extends AbstractPropertyModel<C> implements IWrapModel<C>{
        private static final long serialVersionUID=1L;
        private final Component owner;
        public AttachedCompoundPropertyModel(final Component owner){
            super(CompoundPropertyModel.this);
            this.owner=owner;
        }
        protected String propertyExpression(){
            return CompoundPropertyModel.this.propertyExpression(this.owner);
        }
        public IModel<T> getWrappedModel(){
            return (IModel<T>)CompoundPropertyModel.this;
        }
        public void detach(){
            super.detach();
            CompoundPropertyModel.this.detach();
        }
    }
}
