package org.apache.wicket.markup;

import org.apache.wicket.*;

public interface IMarkupCache{
    void clear();
    Markup getMarkup(MarkupContainer p0,Class<?> p1,boolean p2);
    IMarkupFragment removeMarkup(String p0);
    int size();
    void shutdown();
}
