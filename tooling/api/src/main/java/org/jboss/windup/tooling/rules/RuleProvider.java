package org.jboss.windup.tooling.rules;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public interface RuleProvider extends Serializable
{
    /**
     * Contains a version field used for conflict resolution.
     */
    int getVersion();

    /**
     * Contains a version field used for conflict resolution.
     */
    void setVersion(int version);

    /**
     * Contains the ID from the Rule Provider. This should be unique across all rule providers.
     */
    String getProviderID();

    /**
     * Contains the ID from the Rule Provider. This should be unique across all rule providers.
     */
    void setProviderID(String providerID);

    /**
     * Contains the origin of the Rule Provider. For an XML File, this will be a full path to the file.
     */
    String getOrigin();

    /**
     * Contains the origin of the Rule Provider. For an XML File, this will be a full path to the file.
     */
    void setOrigin(String origin);

    /**
     * Contains a human readable description of this rule provider.
     */
    String getDescription();

    /**
     * Contains a human readable description of this rule provider.
     */
    void setDescription(String description);

    /**
     * Contains the phase during which this rule will execute.
     */
    String getPhase();

    /**
     * Contains the phase during which this rule will execute.
     */
    void setPhase(String phase);

    /**
     * Contains the time that this rule's metadata was loaded into the windup-web database.
     */
    Calendar getDateLoaded();

    /**
     * Contains the time that this rule's metadata was loaded into the windup-web database.
     */
    void setDateLoaded(Calendar dateLoaded);

    /**
     * Contains the time that this rule's metadata was last modified on disk. This may be null if no modification date could be determined.
     */
    Calendar getDateModified();

    /**
     * Contains the time that this rule's metadata was last modified on disk. This may be null if no modification date could be determined.
     */
    void setDateModified(Calendar dateModified);

    /**
     * Contains the source technologies for this provider.
     */
    Set<Technology> getSources();

    /**
     * Contains the source technologies for this provider.
     */
    void setSources(Set<Technology> sources);

    /**
     * Contains the target technologies for this provider.
     */
    Set<Technology> getTargets();

    /**
     * Contains the source technologies for this provider.
     */
    void setTargets(Set<Technology> targets);

    /**
     * Contains the list of rules that were loaded by this provider.
     */
    List<Rule> getRules();

    /**
     * Contains the list of rules that were loaded by this provider.
     */
    void setRules(List<Rule> rules);

    /**
     * Contains the path in which this provider was found.
     */
    RulesPath getRulesPath();

    /**
     * Contains the path in which this provider was found.
     */
    void setRulesPath(RulesPath rulesPath);

    /**
     * Contains the type of provider (for example, Java vs Groovy).
     */
    RuleProviderType getRuleProviderType();

    /**
     * Contains the type of provider (for example, Java vs Groovy).
     */
    void setRuleProviderType(RuleProviderType ruleProviderType);

    enum RuleProviderType
    {
        JAVA, XML, GROOVY
    }
}
