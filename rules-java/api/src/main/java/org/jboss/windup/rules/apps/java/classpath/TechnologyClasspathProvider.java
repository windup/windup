package org.jboss.windup.rules.apps.java.classpath;

import org.jboss.windup.config.metadata.TechnologyReference;

import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyClasspathProvider {
    List<Path> getAdditionalClasspaths(TechnologyReference reference);
}
