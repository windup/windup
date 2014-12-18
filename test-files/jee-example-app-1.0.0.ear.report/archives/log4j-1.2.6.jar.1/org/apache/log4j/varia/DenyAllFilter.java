package org.apache.log4j.varia;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.Filter;

public class DenyAllFilter extends Filter{
    public String[] getOptionStrings(){
        return null;
    }
    public void setOption(final String key,final String value){
    }
    public int decide(final LoggingEvent event){
        return -1;
    }
}
