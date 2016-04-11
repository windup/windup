package org.jboss.windup.config.loader;

import org.jboss.windup.config.RuleProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Contains a unique key with the {@link RuleProvider} id and the {@link Rule} id.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class RuleKey {
    private String providerID;
    private String ruleID;

    /**
     * Constructs an instance with the provided providerID and ruleID.
     */
    public RuleKey(String providerID, String ruleID) {
        this.providerID = providerID;
        this.ruleID = ruleID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleKey)) return false;

        RuleKey ruleKey = (RuleKey) o;

        if (providerID != null ? !providerID.equals(ruleKey.providerID) : ruleKey.providerID != null) return false;
        return ruleID != null ? ruleID.equals(ruleKey.ruleID) : ruleKey.ruleID == null;

    }

    @Override
    public int hashCode() {
        int result = providerID != null ? providerID.hashCode() : 0;
        result = 31 * result + (ruleID != null ? ruleID.hashCode() : 0);
        return result;
    }
}
