package org.apache.wicket.util.resource.locator;

import org.apache.wicket.util.file.*;
import org.apache.wicket.util.resource.*;

public class OsgiResourceStreamLocator extends ResourceStreamLocator{
    public OsgiResourceStreamLocator(){
        super();
    }
    public OsgiResourceStreamLocator(final IResourceFinder finder){
        super(finder);
    }
    public IResourceStream locate(final Class<?> clazz,final String path){
        return super.locate(clazz,"/"+path);
    }
}
