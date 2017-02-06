package org.jboss.windup.tooling.rules;

public class RuleImpl implements Rule
{
    private static final long serialVersionUID = 1L;
    private int version;
    private String ruleID;
    private String ruleContents;

    @Override
    public int getVersion()
    {
        return version;
    }

    /**
     * Contains a value used for conflict resolution during concurrent updates.
     */
    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public String getRuleID()
    {
        return ruleID;
    }

    /**
     * Contains the unique identifier of this rule within the provider. This is only guaranteed to be unique within the context of a single Rule
     * provider.
     */
    public void setRuleID(String ruleID)
    {
        this.ruleID = ruleID;
    }

    @Override
    public String getRuleContents()
    {
        return ruleContents;
    }

    /**
     * This contains the text of the rule itself. In the case of XML rules, this will be the literal text. In the case of Java rules, this will be a
     * readable approximation of the rule itself.
     */
    public void setRuleContents(String ruleContents)
    {
        this.ruleContents = ruleContents;
    }
}
