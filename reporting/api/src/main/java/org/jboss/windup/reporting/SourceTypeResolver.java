package org.jboss.windup.reporting;

import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Provides a way for addons to provide a source type for each FileModel type (eg, "java" for java types, "xml" for xml,
 * properties for properties files, etc).
 * 
 * The default implementation resolves some of these common types, and addons may provide additional resolvers.
 * 
 */
public interface SourceTypeResolver
{
    String resolveSourceType(FileModel f);
}
