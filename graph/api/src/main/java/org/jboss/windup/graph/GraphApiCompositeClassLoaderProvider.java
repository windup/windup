package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonDependency;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.windup.util.FurnaceCompositeClassLoader;

/**
 * Provides a composite classloader of all addons that depend on the graph addon.
 * 
 */
public class GraphApiCompositeClassLoaderProvider
{
    @Inject
    private Addon addon;

    @Inject
    private Furnace furnace;

    /**
     * Creates a classloader which combines classloaders of all addons depending on Graph API. This insures that
     * FramedGraph can always load all the relevant types of *Model classes (as all model classes will be in Addons that
     * depend on Graph API).
     */
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
