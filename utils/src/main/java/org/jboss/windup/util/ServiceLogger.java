package org.jboss.windup.util;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.util.Weighted;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public final class ServiceLogger {
    private static final String NEWLINE = OperatingSystemUtils.getLineSeparator();

    /**
     * Log the list of service implementations to the given {@link Logger}.
     */
    public static void logLoadedServices(Logger log, Class<?> type, List<?> services) {
        log.info("Loaded [" + services.size() + "] " + type.getName() + " [" + NEWLINE
                + joinTypeNames(new ArrayList<>(services)) + "]");
    }

    private static String joinTypeNames(final List<?> list) {
        StringBuilder result = new StringBuilder();
        for (Object service : list) {
            // Remove the Javassist suffix.
            result.append("\t").append(service.getClass().getName().replaceAll("_\\$\\$_.*$", ""));
            if (service instanceof Weighted) {
                result.append("<" + ((Weighted) service).priority() + ">");
            }
            result.append(NEWLINE);
        }
        return result.toString();
    }
}