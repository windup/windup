package org.apache.wicket.model;

import org.apache.wicket.*;

public interface IModelComparator extends IClusterable{
    boolean compare(Component p0,Object p1);
}
