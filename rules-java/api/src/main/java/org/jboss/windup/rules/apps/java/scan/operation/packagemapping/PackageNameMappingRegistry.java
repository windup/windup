package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.PathUtil;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleVisit;
import org.ocpsoft.rewrite.util.Visitor;

/**
 * This registry is conceptually similar to the {@link RuleProviderRegistryCache} except that it is designed for {@link PackageNameMapping}. It allows
 * callers (such as bootstrap) to find packagename mappings without fully running Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PackageNameMappingRegistry
{
    private static Logger LOG = Logger.getLogger(PackageNameMappingRegistry.class.getSimpleName());

    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private RuleProviderRegistryCache cache;
    private GraphRewrite event;

    public String getOrganizationForPackage(String packageName)
    {
        return PackageNameMapping.getOrganizationForPackage(this.event, packageName);
    }

    public void loadPackageMappings()
    {
        try (GraphContext graphContext = graphContextFactory.create())
        {
            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
            FileModel windupRulesPath = new FileService(graphContext).createByFilePath(PathUtil.getWindupRulesDir().toString());
            configurationModel.addUserRulesPath(windupRulesPath);

            RuleProviderRegistry registry = cache.getRuleProviderRegistry(graphContext);
            this.event = new GraphRewrite(graphContext);
            RuleSubset ruleSubset = RuleSubset.create(registry.getConfiguration());
            new RuleVisit(ruleSubset).accept(new Visitor<Rule>()
            {
                @Override
                public void visit(Rule r)
                {
                    if (r instanceof PackageNameMapping)
                    {
                        ((PackageNameMapping) r).preRulesetEvaluation(event);
                    }
                }
            });

            graphContext.clear();
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING, "Failed to load rule information due to: " + e.getMessage(), e);
        }
    }
}
