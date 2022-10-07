package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;

/**
 * Implements a handler for:
 *
 * <pre>
 *     &lt;sourceTechnology id="proprietaryserver" versionRange="(1,12]"/&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = MetadataSourceTechnologyHandler.METADATA_SOURCE_TECHNOLOGY_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataSourceTechnologyHandler extends MetadataTechnologyHandler {
    public static final String METADATA_SOURCE_TECHNOLOGY_ELEMENT = "sourceTechnology";
}
