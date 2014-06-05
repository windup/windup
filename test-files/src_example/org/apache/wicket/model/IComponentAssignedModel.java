package org.apache.wicket.model;

import org.apache.wicket.*;

public interface IComponentAssignedModel<T> extends IModel<T>{
    IWrapModel<T> wrapOnAssignment(Component p0);
}
