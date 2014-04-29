package org.jboss.windup.addon.config.furnace;

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
        if (furnace != null && furnace.getStatus().isStarted())
        {
            Set<Class<T>> types = furnace.getAddonRegistry().getExportedTypes(type);
            result.addAll(types);
        }

        return result;

    }
}