package org.apache.log4j.spi;

import org.apache.log4j.spi.LoggerRepository;

public interface RepositorySelector{
    LoggerRepository getLoggerRepository();
}
