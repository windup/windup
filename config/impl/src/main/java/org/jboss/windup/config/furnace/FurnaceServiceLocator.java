package org.jboss.windup.config.furnace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;
import org.ocpsoft.common.spi.ServiceLocator;

public class FurnaceServiceLocator implements ServiceLocator
{

    @Override
    public <T> Collection<Class<T>> locate(final Class<T> type)
    {
        List<Class<T>> result = new ArrayList<>();

        Furnace furnace = FurnaceHolder.getFurnace();
        
        // Furnace may be not available if the ServiceLoader is called before FurnaceHolder 
        // has received the Furnace PostConstruct event, so check for null and if it isStarted
        if (furnace != null && furnace.getStatus().isStarted())
        {
            Set<Class<T>> types = furnace.getAddonRegistry().getExportedTypes(type);
            result.addAll(types);
        }

        return result;

    }
}