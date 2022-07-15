package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;

/**
 * Implements a handler for:
 *
 * <pre>
 *     &lt;targetTechnology id="proprietaryserver" versionRange="(1,12]"/&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = MetadataTargetTechnologyHandler.METADATA_TARGET_TECHNOLOGY_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataTargetTechnologyHandler extends MetadataTechnologyHandler {
    public static final String METADATA_TARGET_TECHNOLOGY_ELEMENT = "targetTechnology";
}
