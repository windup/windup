package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphRule;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Maps from a package pattern (regular expression) to a organization name.
 */
public class PackageNameMapping extends GraphRule implements PackageNameMappingWithPackagePattern, PreRulesetEvaluation
{
    private String organization;
    private String packagePattern;

    /**
     * Gets the organization for the given package (or Maven group id).
     */
    public static String getOrganizationForPackage(GraphRewrite event, String pkg)
    {
        String organization = null;
        for (Map.Entry<Pattern, String> entry : getMappings(event).entrySet())
        {
            Pattern packagePattern = entry.getKey();
            if (packagePattern.matcher(pkg).find())
            {
                organization = entry.getValue();
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
        Pattern pattern;
        if (!packagePattern.startsWith("^"))
        {
            pattern = Pattern.compile("^" + packagePattern);
        }
        else
        {
            pattern = Pattern.compile(packagePattern);
        }

        PackageNameMapping.getMappings(event).put(pattern, organization);
    }

    private static Map<Pattern, String> getMappings(GraphRewrite event)
    {
        @SuppressWarnings("unchecked")
        Map<Pattern, String> map = (Map<Pattern, String>) event.getRewriteContext().get(PackageNameMapping.class);
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
        return true;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
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
        return this.getClass().getName() + "_" + UUID.randomUUID().toString();
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
