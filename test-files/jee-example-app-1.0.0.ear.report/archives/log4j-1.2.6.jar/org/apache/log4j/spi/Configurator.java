package org.apache.log4j.spi;

import org.apache.log4j.spi.LoggerRepository;
import java.net.URL;

public interface Configurator{
    public static final String INHERITED="inherited";
    public static final String NULL="null";
    void doConfigure(URL p0,LoggerRepository p1);
}
