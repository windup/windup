package org.apache.wicket.util.resource.locator.caching;

import java.net.*;
import org.apache.wicket.util.resource.*;

class UrlResourceStreamReference extends AbstractResourceStreamReference{
    private final URL url;
    UrlResourceStreamReference(final UrlResourceStream urlResourceStream){
        super();
        this.url=urlResourceStream.getURL();
        this.saveResourceStream((IResourceStream)urlResourceStream);
    }
    public UrlResourceStream getReference(){
        final UrlResourceStream resourceStream=new UrlResourceStream(this.url);
        this.restoreResourceStream((IResourceStream)resourceStream);
        return resourceStream;
    }
}
