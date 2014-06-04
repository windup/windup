package org.apache.wicket.model;

import org.apache.wicket.*;

public interface IComponentInheritedModel<T> extends IModel<T>{
     <W> IWrapModel<W> wrapOnInheritance(Component p0);
}
