package org.jboss.windup.rules.apps.yaml;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.rules.apps.yaml.model.YamlFileModel;

/**
 * Resolves yaml-related sources to their type for reporting purposes.
 */
public class YamlSourceTypeResolver implements SourceTypeResolver {
    @Override
    public String resolveSourceType(FileModel f) {
        if (f instanceof YamlFileModel) {
            return "yaml";
        } else {
            return null;
        }
    }
}
