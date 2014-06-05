package org.apache.wicket;

import org.apache.wicket.util.convert.*;

public interface IConverterLocator extends IClusterable{
     <C> IConverter<C> getConverter(Class<C> p0);
}
