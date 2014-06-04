package org.apache.wicket.model;

import org.apache.wicket.*;

public class ResourceModel extends AbstractReadOnlyModel<String> implements IComponentAssignedModel<String>{
    private static final long serialVersionUID=1L;
    private final String resourceKey;
    private final String defaultValue;
    public ResourceModel(final String resourceKey){
        this(resourceKey,null);
    }
    public ResourceModel(final String resourceKey,final String defaultValue){
        super();
        this.resourceKey=resourceKey;
        this.defaultValue=defaultValue;
    }
    public String getObject(){
        return Application.get().getResourceSettings().getLocalizer().getString(this.resourceKey,null,this.defaultValue);
    }
    public IWrapModel<String> wrapOnAssignment(final Component component){
        return new AssignmentWrapper(this.resourceKey,this.defaultValue,component);
    }
    private class AssignmentWrapper extends ResourceModel implements IWrapModel<String>{
        private static final long serialVersionUID=1L;
        private final Component component;
        public AssignmentWrapper(final String resourceKey,final String defaultValue,final Component component){
            super(resourceKey,defaultValue);
            this.component=component;
        }
        public IModel<String> getWrappedModel(){
            return ResourceModel.this;
        }
        public String getObject(){
            return Application.get().getResourceSettings().getLocalizer().getString(ResourceModel.this.resourceKey,this.component,ResourceModel.this.defaultValue);
        }
        public void detach(){
            super.detach();
            ResourceModel.this.detach();
        }
    }
}
