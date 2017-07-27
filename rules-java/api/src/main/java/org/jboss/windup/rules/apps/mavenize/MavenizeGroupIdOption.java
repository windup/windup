package org.jboss.windup.rules.apps.mavenize;


import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.util.Util;

/**
 * Gives the user the option to skip class not found analysis.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class MavenizeGroupIdOption extends AbstractConfigurationOption
{
    public static final String NAME = "mavenizeGroupId";
    public static final String REGEX_GROUP_ID = "[a-zA-Z][-_a-zA-Z0-9]*(\\.[a-zA-Z][-_a-zA-Z0-9]*)*";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "The <groupId> to use for Maven project stub pom.xml's.";
    }

    @Override
    public String getDescription()
    {
        return "All pom.xml files will use this value as their <groupId>. If the parameter"
            + " is omitted, " + Util.WINDUP_BRAND_NAME_ACRONYM + " tries to guess some value based on the application,"
            + " but this guess may be wrong. Last resort default value is '"+ModuleAnalysisHelper.LAST_RESORT_DEFAULT_GROUP_ID+"'.";
    }

    @Override
    public Class<?> getType()
    {
        return String.class;
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
        if (value == null)
            return ValidationResult.SUCCESS;

        if (!(value instanceof String))
            return new ValidationResult(ValidationResult.Level.ERROR, NAME + " option must be a String, was: " + value.getClass().getName());

        String strValue = value.toString();
        if (!strValue.matches(REGEX_GROUP_ID))
            return new ValidationResult(ValidationResult.Level.ERROR, "Must follow the Maven groupId format - e.g. 'com.mycompany.groupId'.");

        return ValidationResult.SUCCESS;
    }
}
