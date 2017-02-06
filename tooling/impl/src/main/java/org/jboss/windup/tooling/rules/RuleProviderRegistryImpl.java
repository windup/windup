package org.jboss.windup.tooling.rules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.phase.MigrationRulesPhase;

public class RuleProviderRegistryImpl implements RuleProviderRegistry
{
    private static final long serialVersionUID = 1L;

    private List<RuleProvider> ruleProviders = new ArrayList<>();

    @Override
    public List<RuleProvider> getRuleProviders()
    {
        return ruleProviders;
    }

    public void buildRuleProviders(org.jboss.windup.config.metadata.RuleProviderRegistry registry)
    {
        for (org.jboss.windup.config.RuleProvider provider : registry.getProviders())
        {
            RuleProviderMetadata ruleProviderMetadata = provider.getMetadata();

            String providerID = ruleProviderMetadata.getID();
            String origin = ruleProviderMetadata.getOrigin();

            RuleProvider ruleProvider = new RuleProviderImpl();
            ruleProvider.setProviderID(providerID);
            ruleProvider.setDateLoaded(new GregorianCalendar());
            ruleProvider.setDescription(ruleProviderMetadata.getDescription());
            ruleProvider.setOrigin(origin);

            ruleProviders.add(ruleProvider);

            setFileMetaData(ruleProvider);

            ruleProvider.setSources(technologyReferencesToTechnologyList(ruleProviderMetadata.getSourceTechnologies()));
            ruleProvider.setTargets(technologyReferencesToTechnologyList(ruleProviderMetadata.getTargetTechnologies()));

            String phase = MigrationRulesPhase.class.getSimpleName().toUpperCase();
            if (ruleProviderMetadata.getPhase() != null)
                phase = ruleProviderMetadata.getPhase().getSimpleName().toUpperCase();

            ruleProvider.setPhase(phase);

            ruleProvider.setRuleProviderType(getProviderType(origin));

            List<Rule> rules = new ArrayList<>();

            for (org.ocpsoft.rewrite.config.Rule rule : registry.getRules(provider))
            {
                String ruleID = rule.getId();
                String ruleString = RuleUtils.ruleToRuleContentsString(rule, 0);

                Rule ruleCopy = new RuleImpl();
                ruleCopy.setRuleID(ruleID);
                ruleCopy.setRuleContents(ruleString);
                rules.add(ruleCopy);
            }
            ruleProvider.setRules(rules);
        }
    }

    private Set<Technology> technologyReferencesToTechnologyList(Collection<TechnologyReference> technologyReferences)
    {
        Set<Technology> results = new HashSet<>();
        for (TechnologyReference technologyReference : technologyReferences)
        {
            Technology technology = new TechnologyImpl();
            technology.setName(technologyReference.getId());
            String versionRange = technologyReference.getVersionRangeAsString();
            if (StringUtils.isNotBlank(versionRange))
                technology.setVersionRange(versionRange);
            results.add(technology);
        }
        return results;
    }

    private void setFileMetaData(RuleProvider ruleProvider)
    {
        if (ruleProvider.getOrigin() == null)
            return;

        try
        {
            String filePathString = ruleProvider.getOrigin();

            if (filePathString.startsWith("file:"))
                filePathString = filePathString.substring(5);

            Path filePath = Paths.get(filePathString);
            if (!Files.isRegularFile(filePath))
                return;

            FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(filePathString));
            GregorianCalendar lastModifiedCalendar = new GregorianCalendar();
            lastModifiedCalendar.setTimeInMillis(lastModifiedTime.toMillis());
            ruleProvider.setDateModified(lastModifiedCalendar);

            // TODO: Can we still find the rules path in order to get the relative path?
            // filePath = Paths.get(ruleProvider.getRulesPath().getPath()).relativize(Paths.get(filePathString));
            ruleProvider.setOrigin(filePath.toString());
        }
        catch (Exception e)
        {
            // not a file path... ignore
        }
    }

    private RuleProvider.RuleProviderType getProviderType(String origin)
    {
        if (origin == null)
            return RuleProvider.RuleProviderType.JAVA;
        else if (origin.startsWith("file:") && origin.endsWith(".windup.xml"))
            return RuleProvider.RuleProviderType.XML;
        else if (origin.startsWith("file:") && origin.endsWith(".windup.groovy"))
            return RuleProvider.RuleProviderType.GROOVY;
        else
            return RuleProvider.RuleProviderType.JAVA;
    }
}
