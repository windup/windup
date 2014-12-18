package org.apache.log4j.spi;

import org.apache.log4j.or.ObjectRenderer;
import org.apache.log4j.or.RendererMap;

public interface RendererSupport{
    RendererMap getRendererMap();
    void setRenderer(Class p0,ObjectRenderer p1);
}
