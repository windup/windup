package org.apache.wicket.validation;

import org.apache.wicket.*;

public interface IValidationError extends IClusterable{
    String getErrorMessage(IErrorMessageSource p0);
}
