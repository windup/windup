package org.jboss.windup.rules.apps.java;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaTechnologyMetadata extends TechnologyMetadata
{
    private final Set<Path> additionalClasspaths = new HashSet<>();

    public JavaTechnologyMetadata(TechnologyReference technology)
    {
        super(technology);
    }

    public void addAdditionalClasspath(Path classpath)
    {
        additionalClasspaths.add(classpath);
    }

    public Set<Path> getAdditionalClasspaths()
    {
        return additionalClasspaths;
    }

    @Override
    public boolean handles(TechnologyReference technology)
    {
        return StringUtils.equals(super.getTechnology().getId(), technology.getId());
    }
}
