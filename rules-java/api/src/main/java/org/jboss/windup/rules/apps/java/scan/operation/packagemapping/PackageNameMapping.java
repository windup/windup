package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphRule;
import org.jboss.windup.config.PreRulesetEvaluation;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.PathUtil;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import static java.lang.String.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Maps from a package to a organization name.
 */
public class PackageNameMapping extends GraphRule implements PackageNameMappingWithPackagePattern, PackageNameMappingWithOrganization, PreRulesetEvaluation
{
    private static final Logger LOG = Logger.getLogger(PackageNameMapping.class.getName());

    private String id = this.getClass().getName() + "_" + UUID.randomUUID();

    private String organization;
    private String packagePattern;

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

    @Override
    public void preRulesetEvaluation(GraphRewrite event)
    {
        PackageNameMapping.getMappings(event).put(packagePattern, organization);
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

    @Override
    public String getId()
    {
        return id;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getPackagePattern()
    {
        return packagePattern;
    }

    public void setPackagePattern(String packagePattern)
    {
        this.packagePattern = packagePattern;
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
     * Indicates that all packages within an archive are either "known" by the package mapper or none of the
     * packages are in the list of packages given by the client. Generally this indicates that the archive
     * does not contain customer code.
     */
    public static boolean areAllPackagesKnown(GraphRewrite event, String filePath)
    {
        if (!equalsIgnoreCase(substringAfterLast(filePath, "."), "jar"))
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
        boolean customerPackagesSpecified = !javaConfigurationModel.getScanJavaPackages().isEmpty();

        // this should only be true if:
        // 1) the package does not contain *any* customer packages.
        // 2) the package contains "known" vendor packages.
        boolean containsOnlyKnownPackages = false;

        String organization = null;
        Enumeration<?> e = archive.entries();

        // go through all entries...
        ZipEntry entry;
        while (e.hasMoreElements())
        {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() || !endsWith(entryName, ".class"))
                continue;

            String classname = PathUtil.classFilePathToClassname(entryName);
            if (!containsOnlyKnownPackages)
            {
                organization = getOrganizationFromMappings(event, classname);
                if (organization != null)
                {
                    containsOnlyKnownPackages = true;
                } else
                {
                    // we couldn't find a package definitively, so ignore the archive
                    break;
                }
            }

            // If the user specified package names and this is in those package names, then scan it anyway
            if (customerPackagesSpecified && javaConfigurationService.shouldScanPackage(classname))
            {
                return false;
            }
        }

        if (containsOnlyKnownPackages)
            LOG.info("Known Package: " + archive.getName() + "; Organization: " + organization);

        return containsOnlyKnownPackages;
    }

    /**
     * Tries to extract the organization of the given package using the existing mappings
     */
    public static String getOrganizationFromMappings(GraphRewrite event, String pkg)
    {
        final String fullPkg = pkg + ".";
        String organization = null;
        // For all the mappings, see if this package matches any of them
        for (String knownPkg : new ArrayList<>(getMappings(event).keySet()))
        {
            final String knownPkgPrefix = knownPkg + ".";
            if (startsWith(fullPkg, knownPkgPrefix))
            {
                organization = knownPkg;
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(format(" -- Found organization for package %s: %s", pkg, organization));
                }
                break;
            }
        }
        return organization;
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

}
