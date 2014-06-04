package org.apache.wicket.validation;

import org.apache.wicket.model.*;

public interface IValidatable<T>{
    T getValue();
    void error(IValidationError p0);
    boolean isValid();
    IModel<T> getModel();
}
