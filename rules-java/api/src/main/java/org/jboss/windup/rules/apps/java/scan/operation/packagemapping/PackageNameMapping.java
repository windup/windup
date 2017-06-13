package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphRule;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.PathUtil;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Maps from a package to a organization name.
 */
public class PackageNameMapping extends GraphRule implements PackageNameMappingWithPackagePattern, PackageNameMappingWithOrganization, PreRulesetEvaluation
{
    private static final Logger LOG = Logger.getLogger(PackageNameMapping.class.getName());

    private String id = this.getClass().getName() + "_" + UUID.randomUUID().toString();

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

    @Override
    public Rule withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Sets the organization to map the package to.
     */
    @Override
    public PackageNameMappingWithOrganization toOrganization(String organization)
    {
        setOrganization(organization);
        return this;
    }

    /**
     * Indicates that all of the packages within an archive are "known" by the package mapper. Generally
     * this indicates that the archive does not contain customer code.
     */
    public static boolean isExclusivelyKnownArchive(GraphRewrite event, String filePath)
    {
        String extension = StringUtils.substringAfterLast(filePath, ".");

        if (!StringUtils.equalsIgnoreCase(extension, "jar"))
            return false;

        ZipFile archive;
        try
        {
            archive = new ZipFile(filePath);
        } catch (IOException e)
        {
            return false;
        }

        WindupJavaConfigurationService javaConfigurationService = new WindupJavaConfigurationService(event.getGraphContext());
        WindupJavaConfigurationModel javaConfigurationModel = WindupJavaConfigurationService.getJavaConfigurationModel(event.getGraphContext());

        // indicates that the user did specify some packages to scan explicitly (as opposed to scanning everything)
        boolean customerPackagesSpecified = javaConfigurationModel.getScanJavaPackages().iterator().hasNext();

        // this should only be true if:
        // 1) the package does not contain *any* customer packages.
        // 2) the package contains "known" vendor packages.
        boolean exclusivelyKnown = false;

        String organization = null;
        Enumeration<?> e = archive.entries();

        // go through all entries...
        ZipEntry entry;
        while (e.hasMoreElements())
        {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() || !StringUtils.endsWith(entryName, ".class"))
                continue;

            String classname = PathUtil.classFilePathToClassname(entryName);
            // if the package isn't current "known", try to match against known packages for this entry.
            if (!exclusivelyKnown)
            {
                organization = getOrganizationForPackage(event, classname);
                if (organization != null)
                {
                    exclusivelyKnown = true;
                } else
                {
                    // we couldn't find a package definitively, so ignore the archive
                    exclusivelyKnown = false;
                    break;
                }
            }

            // If the user specified package names and this is in those package names, then scan it anyway
            if (customerPackagesSpecified && javaConfigurationService.shouldScanPackage(classname))
            {
                return false;
            }
        }

        if (exclusivelyKnown)
            LOG.info("Known Package: " + archive.getName() + "; Organization: " + organization);

        // Return the evaluated exclusively known value.
        return exclusivelyKnown;
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
