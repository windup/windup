package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonDependency;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.windup.util.FurnaceCompositeClassLoader;

public class GraphApiCompositeClassLoaderProvider
{
    @Inject
    private Addon addon;

    @Inject
    private Furnace furnace;

    public ClassLoader getCompositeClassLoader()
    {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                return addonDependsOnGraphApi(addon);
            }
        };

        for (Addon addon : furnace.getAddonRegistry().getAddons(filter))
        {
            loaders.add(addon.getClassLoader());
        }

        return new FurnaceCompositeClassLoader(getClass().getClassLoader(), loaders);
    }

    private boolean addonDependsOnGraphApi(Addon addon)
    {
        for (AddonDependency dep : addon.getDependencies())
        {
            if (dep.getDependency().equals(this.addon))
            {
                return true;
            }
            boolean subDep = addonDependsOnGraphApi(dep.getDependency());
            if (subDep)
            {
                return true;
            }
        }
        return false;
    }
}
