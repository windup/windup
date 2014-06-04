package org.apache.wicket.markup;

import org.apache.wicket.*;

public interface IMarkupCacheKeyProvider{
    String getCacheKey(MarkupContainer p0,Class<?> p1);
}
