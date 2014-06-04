package org.apache.wicket.markup.resolver;

import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public interface IComponentResolver extends IClusterable{
    Component resolve(MarkupContainer p0,MarkupStream p1,ComponentTag p2);
}
