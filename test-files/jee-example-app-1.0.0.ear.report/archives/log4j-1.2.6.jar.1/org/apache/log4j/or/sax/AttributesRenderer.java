package org.apache.log4j.or.sax;

import org.xml.sax.Attributes;
import org.apache.log4j.or.ObjectRenderer;

public class AttributesRenderer implements ObjectRenderer{
    public String doRender(final Object o){
        if(o instanceof Attributes){
            final StringBuffer sbuf=new StringBuffer();
            final Attributes a=(Attributes)o;
            final int len=a.getLength();
            boolean first=true;
            for(int i=0;i<len;++i){
                if(first){
                    first=false;
                }
                else{
                    sbuf.append(", ");
                }
                sbuf.append(a.getQName(i));
                sbuf.append('=');
                sbuf.append(a.getValue(i));
            }
            return sbuf.toString();
        }
        return o.toString();
    }
}
