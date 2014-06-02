package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.*;
import org.apache.wicket.model.*;
import java.util.*;
import org.slf4j.*;

public class ServerAndClientTimeFilter implements IResponseFilter{
    private static final Logger log;
    public AppendingStringBuffer filter(final AppendingStringBuffer responseBuffer){
        final int headIndex=responseBuffer.indexOf("<head>");
        final int bodyIndex=responseBuffer.indexOf("</body>");
        final long timeTaken=System.currentTimeMillis()-RequestCycle.get().getStartTime();
        if(headIndex!=-1&&bodyIndex!=-1){
            final Map<String,String> map=(Map<String,String>)new HashMap(4);
            map.put("clienttime","' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
            map.put("servertime",timeTaken/1000.0+"s");
            final AppendingStringBuffer defaultValue=new AppendingStringBuffer(128);
            defaultValue.append("Server parsetime: ");
            defaultValue.append(timeTaken/1000.0);
            defaultValue.append("s, Client parsetime: ' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
            final String txt=Application.get().getResourceSettings().getLocalizer().getString("ServerAndClientTimeFilter.statustext",null,Model.ofMap(map),defaultValue.toString());
            final AppendingStringBuffer endScript=new AppendingStringBuffer(150);
            endScript.append("\n").append("<script type=\"text/javascript\">\n/*<![CDATA[*/\n");
            endScript.append("\nwindow.defaultStatus='");
            endScript.append(txt);
            endScript.append("';\n").append("\n/*]]>*/\n</script>\n").append("\n");
            responseBuffer.insert(bodyIndex-1,(Object)endScript);
            responseBuffer.insert(headIndex+6,"\n<script type=\"text/javascript\">\n/*<![CDATA[*/\n\nvar clientTimeVariable = new Date().getTime();\n\n/*]]>*/\n</script>\n\n");
        }
        ServerAndClientTimeFilter.log.info(timeTaken+"ms server time taken for request "+RequestCycle.get().getRequest().getUrl()+" response size: "+responseBuffer.length());
        return responseBuffer;
    }
    static{
        log=LoggerFactory.getLogger(ServerAndClientTimeFilter.class);
    }
}
