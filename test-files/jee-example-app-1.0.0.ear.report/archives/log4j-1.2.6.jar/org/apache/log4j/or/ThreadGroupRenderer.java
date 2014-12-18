package org.apache.log4j.or;

import org.apache.log4j.Layout;
import org.apache.log4j.or.ObjectRenderer;

public class ThreadGroupRenderer implements ObjectRenderer{
    public String doRender(final Object o){
        if(o instanceof ThreadGroup){
            final StringBuffer sbuf=new StringBuffer();
            final ThreadGroup tg=(ThreadGroup)o;
            sbuf.append("java.lang.ThreadGroup[name=");
            sbuf.append(tg.getName());
            sbuf.append(", maxpri=");
            sbuf.append(tg.getMaxPriority());
            sbuf.append("]");
            final Thread[] t=new Thread[tg.activeCount()];
            tg.enumerate(t);
            for(int i=0;i<t.length;++i){
                sbuf.append(Layout.LINE_SEP);
                sbuf.append("   Thread=[");
                sbuf.append(t[i].getName());
                sbuf.append(",");
                sbuf.append(t[i].getPriority());
                sbuf.append(",");
                sbuf.append(t[i].isDaemon());
                sbuf.append("]");
            }
            return sbuf.toString();
        }
        return o.toString();
    }
}
