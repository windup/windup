package org.jboss.windup.rules.apps.java.archives;

import org.jboss.windup.config.metadata.AbstractRulesetMetadata;

/**
 * MetadataBuilder for the Java Archive Identification addon.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationRulesetMetadata extends AbstractRulesetMetadata {
    public static final String RULE_SET_ID = "JavaArchiveIdentification";

    public ArchiveIdentificationRulesetMetadata() {
        super(RULE_SET_ID);
    }
}
