package org.apache.wicket.validation;

import org.apache.wicket.*;

public interface IValidator<T> extends IClusterable{
    void validate(IValidatable<T> p0);
}
