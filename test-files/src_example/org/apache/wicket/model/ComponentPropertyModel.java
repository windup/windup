package org.apache.wicket.model;

import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class ComponentPropertyModel<T> extends AbstractReadOnlyModel<T> implements IComponentAssignedModel<T>{
    private static final long serialVersionUID=1L;
    private final String propertyName;
    public ComponentPropertyModel(final String propertyName){
        super();
        this.propertyName=propertyName;
    }
    public T getObject(){
        throw new IllegalStateException("Wrapper should have been called");
    }
    public IWrapModel<T> wrapOnAssignment(final Component component){
        return new AssignmentWrapper<T>(component,this.propertyName);
    }
    private class AssignmentWrapper<P> extends AbstractReadOnlyModel<P> implements IWrapModel<P>{
        private static final long serialVersionUID=1L;
        private final Component component;
        private final String propertyName;
        AssignmentWrapper(final Component component,final String propertyName){
            super();
            this.component=component;
            this.propertyName=propertyName;
        }
        public IModel<T> getWrappedModel(){
            return (IModel<T>)ComponentPropertyModel.this;
        }
        protected String propertyExpression(){
            return this.propertyName;
        }
        public P getObject(){
            return (P)PropertyResolver.getValue(this.propertyName,this.component.getParent().getInnermostModel().getObject());
        }
        public void detach(){
            super.detach();
            ComponentPropertyModel.this.detach();
        }
    }
}
