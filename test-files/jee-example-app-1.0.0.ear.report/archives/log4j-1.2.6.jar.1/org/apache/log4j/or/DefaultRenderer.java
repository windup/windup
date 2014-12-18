package org.apache.log4j.or;

import org.apache.log4j.or.ObjectRenderer;

class DefaultRenderer implements ObjectRenderer{
    public String doRender(final Object o){
        return o.toString();
    }
}
