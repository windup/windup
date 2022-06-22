package org.jboss.windup.rules.apps.java;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * Contains metadata about a particular technology. This can include things like additional classpath details
 * that are useful for analyzing the contents of Java source code.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaTechnologyMetadata extends TechnologyMetadata {
    private final Set<Path> additionalClasspaths = new HashSet<>();

    /**
     * Create a new instance for the given {@link TechnologyReference}.
     */
    public JavaTechnologyMetadata(TechnologyReference technology) {
        super(technology);
    }

    /**
     * Adds the provided path to the analysis library path.
     */
    public void addAdditionalClasspath(Path classpath) {
        additionalClasspaths.add(classpath);
    }

    /**
     * Gets classes that should be used for determining class bindings during Java analysis.
     */
    public Set<Path> getAdditionalClasspaths() {
        return additionalClasspaths;
    }

    @Override
    public boolean handles(TechnologyReference technology) {
        return super.getTechnology().matches(technology);
    }
}
