package org.apache.wicket.util.resource.locator.caching;

import org.apache.wicket.util.resource.*;

class NullResourceStreamReference implements IResourceStreamReference{
    static final NullResourceStreamReference INSTANCE;
    public IResourceStream getReference(){
        return null;
    }
    static{
        INSTANCE=new NullResourceStreamReference();
    }
}
