package org.jboss.windup.modulespec;

import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.impl.modules.providers.AbstractModuleSpecProvider;
import org.jboss.modules.ModuleIdentifier;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaGraphDependenciesClasspathSpec extends AbstractModuleSpecProvider {
    public static final ModuleIdentifier ID = ModuleIdentifier.create("furnace.windup.graph.api");

    public static Set<String> paths = new HashSet<>();

    static {
        /*
         *  Add the paths that we need from Sun Java.
         */
        paths.add("com/sun/nio/file");
        paths.add("META-INF/services");
    }

    @Override
    public ModuleIdentifier getId() {
        return ID;
    }

    @Override
    protected Set<String> getPaths() {
        return paths;
    }

}
