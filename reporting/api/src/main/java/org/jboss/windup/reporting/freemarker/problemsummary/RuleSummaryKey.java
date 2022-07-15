package org.jboss.windup.reporting.freemarker.problemsummary;

/**
 * Contains a key that can uniquely identify a RuleSummary by title and rule ID.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RuleSummaryKey {
    private final int effort;
    private final String ruleID;
    private final String title;

    /**
     * Creates a key with the given rule id and title.
     */
    public RuleSummaryKey(Integer effort, String ruleID, String title) {
        this.effort = effort == null ? 0 : effort;
        this.ruleID = ruleID;
        this.title = title;
    }

    /**
     * Gets the rule id.
     */
    public String getRuleID() {
        return ruleID;
    }

    /**
     * Gets the title.
     */
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RuleSummaryKey))
            return false;

        RuleSummaryKey that = (RuleSummaryKey) o;

        if (effort != that.effort)
            return false;
        if (ruleID != null ? !ruleID.equals(that.ruleID) : that.ruleID != null)
            return false;
        return title != null ? title.equals(that.title) : that.title == null;

    }

    @Override
    public int hashCode() {
        int result = effort;
        result = 31 * result + (ruleID != null ? ruleID.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
