package org.apache.wicket.validation;

import java.util.*;

public interface IErrorMessageSource{
    String getMessage(String p0);
    String substitute(String p0,Map<String,Object> p1) throws IllegalStateException;
}
