package org.jboss.windup.tooling.rules;

import java.io.Serializable;

public interface Rule extends Serializable
{
    /**
     * Contains a value used for conflict resolution during concurrent updates.
     */
    int getVersion();

    /**
     * Contains a value used for conflict resolution during concurrent updates.
     */
    void setVersion(int version);

    /**
     * Contains the unique identifier of this rule within the provider. This is only guaranteed to be unique within the context of a single Rule
     * provider.
     */
    String getRuleID();

    /**
     * Contains the unique identifier of this rule within the provider. This is only guaranteed to be unique within the context of a single Rule
     * provider.
     */
    void setRuleID(String ruleID);

    /**
     * This contains the text of the rule itself. In the case of XML rules, this will be the literal text. In the case of Java rules, this will be a
     * readable approximation of the rule itself.
     */
    String getRuleContents();

    /**
     * This contains the text of the rule itself. In the case of XML rules, this will be the literal text. In the case of Java rules, this will be a
     * readable approximation of the rule itself.
     */
    void setRuleContents(String ruleContents);
}
