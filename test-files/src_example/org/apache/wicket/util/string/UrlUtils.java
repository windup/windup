package org.apache.wicket.util.string;

import org.apache.wicket.request.*;
import org.apache.wicket.request.cycle.*;

@Deprecated
public class UrlUtils extends org.apache.wicket.request.UrlUtils{
    @Deprecated
    public static String rewriteToContextRelative(final String url,final RequestCycle requestCycle){
        if(isRelative(url)){
            return requestCycle.getUrlRenderer().renderContextRelativeUrl(url);
        }
        return url;
    }
}
