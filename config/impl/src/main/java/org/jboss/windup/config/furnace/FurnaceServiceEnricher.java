package org.jboss.windup.config.furnace;

import java.util.Collection;
import java.util.HashSet;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.ocpsoft.common.spi.ServiceEnricher;
import org.ocpsoft.logging.Logger;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class FurnaceServiceEnricher implements ServiceEnricher
{
    Logger log = Logger.getLogger(FurnaceServiceEnricher.class);

    @Override
    public <T> Collection<T> produce(final Class<T> type)
    {
        final Collection<T> result = new HashSet<>();
        final Furnace furnace = FurnaceHolder.getFurnace();

        // Furnace may be not available if the ServiceLoader is called before FurnaceHolder
        // has received the Furnace PostConstruct event, so check for null and if it isStarted
        if (furnace != null && furnace.getStatus().isStarted())
        {
            final Imported<T> services = furnace.getAddonRegistry().getServices(type);
            for (final T service : services)
            {
                result.add(service);
            }
        }
        return result;
    }

    @Override
    public <T> void enrich(final T service)
    {
        // no-op. Furnace does not support enriching... directly.
    }

}