package org.jboss.windup.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.util.Weighted;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class ServiceLogger
{

    /**
     * Log the list of service implementations to the given {@link Logger}.
     */
    public static void logLoadedServices(Logger log, Class<?> type, List<?> services)
    {
        log.info("Loaded [" + services.size() + "] " + type.getName() + " [" + OperatingSystemUtils.getLineSeparator()
                    + joinTypeNames(new ArrayList<>(services)) + OperatingSystemUtils.getLineSeparator() + "]");
    }

    private static String joinTypeNames(final List<?> list)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            Object service = list.get(i);
            result.append(service.getClass().getName().replaceAll("_\\$\\$_.*$", ""));
            if (service instanceof Weighted)
            {
                result.append("<" + ((Weighted) service).priority() + ">");
            }
            if ((i + 1) < list.size())
            {
                result.append(OperatingSystemUtils.getLineSeparator());
            }
        }
        return result.toString();
    }
}