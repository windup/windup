package org.jboss.windup.reporting.freemarker;

import java.net.URL;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.furnace.FurnaceHolder;

import freemarker.cache.URLTemplateLoader;

class FurnaceFreeMarkerTemplateLoader extends URLTemplateLoader
{
    @Override
    protected URL getURL(String name)
    {
        URL result = null;
        Furnace furnace = FurnaceHolder.getFurnace();
        for (Addon addon : furnace.getAddonRegistry().getAddons())
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
}
