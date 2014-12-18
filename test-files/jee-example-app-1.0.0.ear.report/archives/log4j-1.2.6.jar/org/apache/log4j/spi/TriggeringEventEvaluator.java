package org.apache.log4j.spi;

import org.apache.log4j.spi.LoggingEvent;

public interface TriggeringEventEvaluator{
    boolean isTriggeringEvent(LoggingEvent p0);
}
