package org.jboss.windup.reporting.freemarker;

import java.net.URL;
import java.util.concurrent.Callable;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.lock.LockMode;
import org.jboss.forge.furnace.util.AddonFilters;
import org.jboss.windup.config.furnace.FurnaceHolder;

import freemarker.cache.URLTemplateLoader;

public class FurnaceFreeMarkerTemplateLoader extends URLTemplateLoader
{
    @Override
    protected URL getURL(final String name)
    {
        final Furnace furnace = FurnaceHolder.getFurnace();
        return furnace.getLockManager().performLocked(LockMode.READ, new Callable<URL>()
        {
            @Override
            public URL call() throws Exception
            {
                URL result = null;
                for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allLoaded()))
                {
                    URL url = addon.getClassLoader().getResource(name);
                    if (url != null)
                    {
                        result = url;
                        break;
                    }
                }
                return result;
            }
        });
    }
}
