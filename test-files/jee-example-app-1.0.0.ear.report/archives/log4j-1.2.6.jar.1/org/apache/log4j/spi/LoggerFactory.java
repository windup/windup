package org.apache.log4j.spi;

import org.apache.log4j.Logger;

public interface LoggerFactory{
    Logger makeNewLoggerInstance(String p0);
}
