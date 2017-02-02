package org.jboss.windup.tooling.rules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleProviderImpl implements RuleProvider 
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private int version;
    private String providerID;

    private String origin;

    private String description;

    private String phase;

    private Calendar dateLoaded;

    private Calendar dateModified;

    private Set<Technology> sources;

    private Set<Technology> targets;

    private List<Rule> rules;

    private RulesPathImpl rulesPath;

    private Set<Tag> tags;

    private RuleProviderType ruleProviderType;

    public RuleProviderImpl()
    {
        this.sources = new HashSet<>();
        this.targets = new HashSet<>();
        this.rules = new ArrayList<>();
        this.tags = new HashSet<>();
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public int getVersion()
    {
        return version;
    }

    @Override
    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public String getProviderID()
    {
        return providerID;
    }

    @Override
    public void setProviderID(String providerID)
    {
        this.providerID = providerID;
    }

    @Override
    public String getOrigin()
    {
        return origin;
    }

    @Override
    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getPhase()
    {
        return phase;
    }

    @Override
    public void setPhase(String phase)
    {
        this.phase = phase;
    }

    @Override
    public Calendar getDateLoaded()
    {
        return dateLoaded;
    }

    @Override
    public void setDateLoaded(Calendar dateLoaded)
    {
        this.dateLoaded = dateLoaded;
    }

    @Override
    public Calendar getDateModified()
    {
        return dateModified;
    }

    @Override
    public void setDateModified(Calendar dateModified)
    {
        this.dateModified = dateModified;
    }

    @Override
    public Set<Technology> getSources()
    {
        return sources;
    }

    @Override
    public void setSources(Set<Technology> sources)
    {
        this.sources = sources;
    }

    @Override
    public Set<Technology> getTargets()
    {
        return targets;
    }

    @Override
    public void setTargets(Set<Technology> targets)
    {
        this.targets = targets;
    }

    @Override
    public List<Rule> getRules()
    {
        return rules;
    }

    @Override
    public void setRules(List<Rule> rules)
    {
        this.rules = rules;
    }

    @Override
    public RulesPathImpl getRulesPath()
    {
        return rulesPath;
    }

	@Override
	public void setRulesPath(RulesPath rulesPath) {
	}

    @Override
    public RuleProviderType getRuleProviderType()
    {
        return ruleProviderType;
    }

    @Override
    public void setRuleProviderType(RuleProviderType ruleProviderType)
    {
        this.ruleProviderType = ruleProviderType;
    }

    @Override
    public Set<Tag> getTags()
    {
        return tags;
    }

    @Override
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }
}
