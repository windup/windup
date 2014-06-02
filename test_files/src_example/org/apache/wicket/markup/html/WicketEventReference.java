package org.apache.wicket.markup.html;

import org.apache.wicket.request.resource.*;

public class WicketEventReference extends JavaScriptResourceReference{
    private static final long serialVersionUID=1L;
    public static final ResourceReference INSTANCE;
    private WicketEventReference(){
        super((Class<?>)WicketEventReference.class,"wicket-event.js");
    }
    static{
        INSTANCE=new WicketEventReference();
    }
}
