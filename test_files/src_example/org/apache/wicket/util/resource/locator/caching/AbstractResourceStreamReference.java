package org.apache.wicket.util.resource.locator.caching;

import java.util.*;
import org.apache.wicket.util.resource.*;

abstract class AbstractResourceStreamReference implements IResourceStreamReference{
    private String style;
    private Locale locale;
    private String variation;
    protected void saveResourceStream(final IResourceStream resourceStream){
        this.style=resourceStream.getStyle();
        this.locale=resourceStream.getLocale();
        this.variation=resourceStream.getVariation();
    }
    protected void restoreResourceStream(final IResourceStream resourceStream){
        resourceStream.setStyle(this.style);
        resourceStream.setLocale(this.locale);
        resourceStream.setVariation(this.variation);
    }
}
