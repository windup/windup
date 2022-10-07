package org.jboss.windup.config;

/**
 * Option to force the analysis of already known libraries. What a "known" library is will depend on the particular
 * implementation.
 *
 * @author <a href="mailto:jleflete@gmail.com">Juan Manuel Leflet Estrada</a>
 */
public class AnalyzeKnownLibrariesOption extends AbstractConfigurationOption {

    public static final String NAME = "analyzeKnownLibraries";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Force analysis of known libraries";
    }

    @Override
    public String getDescription() {
        return "Force the analysis of libraries present in the input application(s) even if they are known to Windup";
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public InputType getUIType() {
        return InputType.SINGLE;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public Object getDefaultValue() {
        return false;
    }

    @Override
    public ValidationResult validate(Object value) {
        return ValidationResult.SUCCESS;
    }
}
