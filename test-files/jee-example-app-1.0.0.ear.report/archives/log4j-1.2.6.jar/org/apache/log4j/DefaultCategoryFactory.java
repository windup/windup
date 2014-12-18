package org.apache.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

class DefaultCategoryFactory implements LoggerFactory{
    public Logger makeNewLoggerInstance(final String name){
        return new Logger(name);
    }
}
