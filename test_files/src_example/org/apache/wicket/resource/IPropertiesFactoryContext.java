package org.apache.wicket.resource;

import org.apache.wicket.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.util.watch.*;

public interface IPropertiesFactoryContext{
    Localizer getLocalizer();
    IResourceStreamLocator getResourceStreamLocator();
    IModificationWatcher getResourceWatcher(boolean p0);
}
