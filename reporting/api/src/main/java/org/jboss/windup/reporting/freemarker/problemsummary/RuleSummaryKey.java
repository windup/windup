package org.jboss.windup.reporting.freemarker.problemsummary;

/**
 * Contains a key that can uniquely identify a RuleSummary by title and rule ID.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleSummaryKey
{
    private final String ruleID;
    private final String title;

    /**
     * Creates a key with the given rule id and title.
     */
    public RuleSummaryKey(String ruleID, String title)
    {
        this.ruleID = ruleID;
        this.title = title;
    }

    /**
     * Gets the rule id.
     */
    public String getRuleID()
    {
        return ruleID;
    }

    /**
     * Gets the title.
     */
    public String getTitle()
    {
        return title;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RuleSummaryKey that = (RuleSummaryKey) o;

        if (ruleID != null ? !ruleID.equals(that.ruleID) : that.ruleID != null)
            return false;
        return !(title != null ? !title.equals(that.title) : that.title != null);

    }

    @Override
    public int hashCode()
    {
        int result = ruleID != null ? ruleID.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
