package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.interpolator.*;
import java.util.*;
import org.slf4j.*;

public class AjaxServerAndClientTimeFilter implements IResponseFilter{
    private static Logger log;
    public AppendingStringBuffer filter(final AppendingStringBuffer responseBuffer){
        final int headIndex=responseBuffer.indexOf("<head>");
        final int bodyIndex=responseBuffer.indexOf("</body>");
        final int ajaxStart=responseBuffer.indexOf("<ajax-response>");
        final int ajaxEnd=responseBuffer.indexOf("</ajax-response>");
        final long timeTaken=System.currentTimeMillis()-RequestCycle.get().getStartTime();
        if(headIndex!=-1&&bodyIndex!=-1){
            final AppendingStringBuffer endScript=new AppendingStringBuffer(150);
            endScript.append("\n").append("<script type=\"text/javascript\">\n/*<![CDATA[*/\n");
            endScript.append("\nwindow.defaultStatus='");
            endScript.append(this.getStatusString(timeTaken,"ServerAndClientTimeFilter.statustext"));
            endScript.append("';\n").append("\n/*]]>*/\n</script>\n").append("\n");
            responseBuffer.insert(bodyIndex-1,(Object)endScript);
            responseBuffer.insert(headIndex+6,"\n<script type=\"text/javascript\">\n/*<![CDATA[*/\n\nvar clientTimeVariable = new Date().getTime();\n\n/*]]>*/\n</script>\n\n");
        }
        else if(ajaxStart!=-1&&ajaxEnd!=-1){
            final AppendingStringBuffer startScript=new AppendingStringBuffer(250);
            startScript.append("<evaluate><![CDATA[window.defaultStatus='");
            startScript.append(this.getStatusString(timeTaken,"ajax.ServerAndClientTimeFilter.statustext"));
            startScript.append("';]]></evaluate>");
            responseBuffer.insert(ajaxEnd,startScript.toString());
            responseBuffer.insert(ajaxStart+15,"<evaluate><![CDATA[clientTimeVariable = new Date().getTime();]]></evaluate>");
        }
        AjaxServerAndClientTimeFilter.log.info(timeTaken+"ms server time taken for request "+RequestCycle.get().getRequest().getUrl()+" response size: "+responseBuffer.length());
        return responseBuffer;
    }
    private String getStatusString(final long timeTaken,final String resourceKey){
        final String txt=Application.get().getResourceSettings().getLocalizer().getString(resourceKey,null,"Server parsetime: ${servertime}, Client parsetime: ${clienttime}");
        final Map<String,String> map=(Map<String,String>)new HashMap(4);
        map.put("clienttime","' + (new Date().getTime() - clientTimeVariable)/1000 +  's");
        map.put("servertime",timeTaken/1000.0+"s");
        return MapVariableInterpolator.interpolate(txt,(Map)map);
    }
    static{
        AjaxServerAndClientTimeFilter.log=LoggerFactory.getLogger(AjaxServerAndClientTimeFilter.class);
    }
}
