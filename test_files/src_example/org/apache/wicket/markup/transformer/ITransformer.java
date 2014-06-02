package org.apache.wicket.markup.transformer;

import org.apache.wicket.*;

public interface ITransformer{
    CharSequence transform(Component p0,CharSequence p1) throws Exception;
}
