package org.apache.wicket.model;

import java.lang.reflect.*;

public interface IPropertyReflectionAwareModel<T> extends IModel<T>{
    Field getPropertyField();
    Method getPropertyGetter();
    Method getPropertySetter();
}
