package org.jboss.windup.decompiler.fernflower;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FernflowerJDKLogger extends IFernflowerLogger {
    private static Logger LOG = Logger.getLogger(FernflowerDecompiler.class.getName());

    @Override
    public void writeMessage(String s, Severity severity) {
        LOG.log(getLevel(severity), s);
    }

    @Override
    public void writeMessage(String s, Severity severity, Throwable throwable) {
        if (s == null)
            LOG.log(getLevel(severity), "Error decompiling due to: " + throwable.getMessage(), throwable);
        else
            LOG.log(getLevel(severity), s, throwable);
    }

    @Override
    public void writeMessage(String s, Throwable throwable) {
        if (s == null)
            LOG.log(Level.SEVERE, "Error decompiling due to: " + throwable.getMessage(), throwable);
        else
            LOG.log(Level.SEVERE, s, throwable);
    }

    private Level getLevel(Severity severity) {
        switch (severity) {
            case TRACE:
                return Level.FINE;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
            default:
                return Level.INFO;
        }
    }
}
