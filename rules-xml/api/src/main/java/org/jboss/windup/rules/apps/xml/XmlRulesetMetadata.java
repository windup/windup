package org.jboss.windup.rules.apps.xml;

import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * Metadata for the XML Rules addon.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XmlRulesetMetadata extends AbstractRulesetMetadata {
    public static final String RULE_SET_ID = "CoreXMLRules";

    public XmlRulesetMetadata() {
        super(RULE_SET_ID);
    }
}
