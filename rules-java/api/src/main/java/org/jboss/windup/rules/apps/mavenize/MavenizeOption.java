package org.jboss.windup.rules.apps.mavenize;


import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Gives the user the option to skip class not found analysis.
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MavenizeOption extends AbstractConfigurationOption
{
    public static final String NAME = "mavenize";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Create a Maven project stub.";
    }

    @Override
    public String getDescription()
    {
        return "Create a Maven project stub (a directory structure with pom.xml files) based on the structure and content of the application."
                + " This helps with locating the libraries and their versions, as well as adding the right Java EE API and creating the correct"
                + "  dependencies between the project modules.";
    }

    @Override
    public Class<?> getType()
    {
        return Boolean.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.SINGLE;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }
}
