package org.jboss.windup.rules.apps.java.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

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
        return "A list of java package name prefixes to scan (eg, com.myapp)";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Java packages to scan.";
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

    public ValidationResult validate(Object value)
    {
        if (isScanPackagesTooGeneral((List<String>) value))
        {
            String message = "No packages were set in --" + ScanPackagesOption.NAME
                + ". This will cause all .jar files to be decompiled and can possibly take a long time. "
                + "Check the Windup User Guide for performance tips.";

            return new ValidationResult(ValidationResult.Level.WARNING, message);
        }

        return ValidationResult.SUCCESS;
    }

    private boolean isScanPackagesTooGeneral(List<String> includeJavaPackages)
    {
        if (includeJavaPackages != null)
        {
            Set<String> tooGeneral = new HashSet(Arrays.asList("com org net".split(" ")));
            for (String pkg : includeJavaPackages)
            {
                if (tooGeneral.contains(pkg))
                    continue;
                return false;
            }
        }

        return true;
    }

}
