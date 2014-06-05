package org.apache.wicket.ajax;

import org.apache.wicket.request.resource.*;

public class WicketAjaxReference extends JavaScriptResourceReference{
    private static final long serialVersionUID=1L;
    public static final ResourceReference INSTANCE;
    private WicketAjaxReference(){
        super((Class<?>)WicketAjaxReference.class,"wicket-ajax.js");
    }
    static{
        INSTANCE=new WicketAjaxReference();
    }
}
