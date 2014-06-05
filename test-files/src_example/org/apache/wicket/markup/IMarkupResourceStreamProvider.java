package org.apache.wicket.markup;

import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;

public interface IMarkupResourceStreamProvider{
    IResourceStream getMarkupResourceStream(MarkupContainer p0,Class<?> p1);
}
