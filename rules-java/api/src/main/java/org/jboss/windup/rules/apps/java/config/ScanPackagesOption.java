package org.jboss.windup.rules.apps.java.config;

import java.util.Arrays;
import java.util.List;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.util.Util;

/**
 * Indicates the Java packages for Windup to scan.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ScanPackagesOption extends AbstractConfigurationOption
{
    public static final String NAME = "packages";

    @Override
    public String getDescription()
    {
        return "A list of java package name prefixes to scan (eg, com.myapp). Multiple items can be separated by a space (eg, --" + NAME
                    + " PACKAGE_1 PACKAGE_2).";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Java packages to scan";
    }

    @Override
    public Class<?> getType()
    {
        return String.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @SuppressWarnings("unchecked")
    public ValidationResult validate(Object value)
    {
        if (packagesNotSpecified((List<String>) value))
        {
            String message = "No packages were set in --" + ScanPackagesOption.NAME
                        + ". This will cause all .jar files to be decompiled and can possibly take a long time. "
                        + "Check the " + Util.WINDUP_BRAND_NAME_ACRONYM + " User Guide for performance tips.";

            return new ValidationResult(ValidationResult.Level.WARNING, message);
        }
        else if (packagesTooGeneral((List<String>) value))
        {
            String message = "The packages specified to scan are very broad. This may cause many .jar files to be "
                        + "decompiled and can possibly take a long time. "
                        + "Check the " + Util.WINDUP_BRAND_NAME_ACRONYM + " User Guide for performance tips.";

            return new ValidationResult(ValidationResult.Level.WARNING, message);
        }

        return ValidationResult.SUCCESS;
    }

    private boolean packagesTooGeneral(List<String> includeJavaPackages)
    {
        List<String> generalPackages = Arrays.asList("com", "org", "net");
        for (String packge : includeJavaPackages)
        {
            if (generalPackages.contains(packge))
                continue;

            return false;
        }
        return true;
    }

    /**
     * @return <code>true</code> if the given packages are too general, or not set at all.
     */
    private boolean packagesNotSpecified(List<String> includeJavaPackages)
    {
        return includeJavaPackages == null || includeJavaPackages.isEmpty();
    }

}
