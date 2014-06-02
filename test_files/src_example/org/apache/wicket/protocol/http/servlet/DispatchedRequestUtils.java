package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import org.apache.wicket.util.string.*;

class DispatchedRequestUtils{
    static String getRequestUri(final HttpServletRequest request,final String attributeName,String filterPrefix){
        if(filterPrefix==null){
            filterPrefix="";
        }
        if(!Strings.isEmpty((CharSequence)filterPrefix)&&!filterPrefix.startsWith("/")){
            filterPrefix='/'+filterPrefix;
        }
        String uri=(String)request.getAttribute(attributeName);
        if(uri!=null&&uri.startsWith(filterPrefix)&&!"/".equals(filterPrefix)){
            uri=uri.substring(filterPrefix.length());
        }
        return uri;
    }
}
