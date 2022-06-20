package org.jboss.windup.rules.apps.java.classpath;

import java.nio.file.Path;
import java.util.List;

import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyClasspathProvider {
    List<Path> getAdditionalClasspaths(TechnologyReference reference);
}
