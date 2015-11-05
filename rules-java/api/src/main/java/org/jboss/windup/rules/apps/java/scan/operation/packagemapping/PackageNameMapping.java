package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphRule;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Maps from a package to a organization name.
 */
public class PackageNameMapping extends GraphRule implements PackageNameMappingWithPackagePattern, PreRulesetEvaluation
{
    private static final Logger LOG = Logger.getLogger(PackageNameMapping.class.getSimpleName());

    private final String id = this.getClass().getName() + "_" + UUID.randomUUID().toString();

    private String organization;
    private String packagePattern;

    /**
     * Gets the organization for the given package (or Maven group id).
     */
    public static String getOrganizationForPackage(GraphRewrite event, String pkg)
    {
        final String pkgComparison = pkg + ".";
        String organization = null;
        for (Map.Entry<String, String> entry : getMappings(event).entrySet())
        {
            final String pkgPattern = entry.getKey() + ".";
            if (StringUtils.startsWith(pkgComparison, pkgPattern))
            {
                organization = entry.getValue();
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(" -- Found organization: " + organization);
                }
                break;
            }
        }
        return organization;
    }

    /**
     * Sets the package pattern to match against.
     */
    public static PackageNameMappingWithPackagePattern fromPackage(String packagePattern)
    {
        PackageNameMapping packageNameMapping = new PackageNameMapping();
        packageNameMapping.setPackagePattern(packagePattern);
        return packageNameMapping;
    }

    /**
     * Sets the organization to map the package to.
     */
    @Override
    public Rule toOrganization(String organization)
    {
        setOrganization(organization);
        return this;
    }

    @Override
    public void preRulesetEvaluation(GraphRewrite event)
    {
        PackageNameMapping.getMappings(event).put(packagePattern, organization);
    }

    private static Map<String, String> getMappings(GraphRewrite event)
    {
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) event.getRewriteContext().get(PackageNameMapping.class);
        if (map == null)
        {
            map = new HashMap<>();
            event.getRewriteContext().put(PackageNameMapping.class, map);
        }
        return map;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        /*
         * This is empty because we only care about PreRulesetEvaluation
         */
        return true;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        /*
         * This is empty because we only care about PreRulesetEvaluation
         */
    }

    /**
     * Contains the organization name.
     */
    public String getOrganization()
    {
        return organization;
    }

    /**
     * Contains the organization name.
     */
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    /**
     * Contains the package pattern.
     */
    public String getPackagePattern()
    {
        return packagePattern;
    }

    /**
     * Contains the package pattern.
     */
    public void setPackagePattern(String packagePattern)
    {
        this.packagePattern = packagePattern;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(".fromPackage(" + packagePattern + ")");
        builder.append(".toOrganization(" + organization + ")");
        return builder.toString();
    }
}
