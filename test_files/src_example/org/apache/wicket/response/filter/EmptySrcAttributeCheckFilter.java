package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class EmptySrcAttributeCheckFilter implements IResponseFilter{
    private static final Logger log;
    public static final EmptySrcAttributeCheckFilter INSTANCE;
    public AppendingStringBuffer filter(final AppendingStringBuffer responseBuffer){
        int pos=responseBuffer.indexOf("src=\"\"");
        if(pos<0){
            pos=responseBuffer.indexOf("src=''");
            if(pos<0){
                pos=responseBuffer.indexOf("src=\"#\"");
                if(pos<0){
                    pos=responseBuffer.indexOf("src='#'");
                }
            }
        }
        if(pos>=0){
            EmptySrcAttributeCheckFilter.log.warn("Empty src attribute found in response:");
            final int from=Math.max(0,pos-32);
            final int to=Math.min(pos+32,responseBuffer.length());
            EmptySrcAttributeCheckFilter.log.warn("[...]"+responseBuffer.substring(from,to)+"[...]");
        }
        return responseBuffer;
    }
    static{
        log=LoggerFactory.getLogger(EmptySrcAttributeCheckFilter.class);
        INSTANCE=new EmptySrcAttributeCheckFilter();
    }
}
