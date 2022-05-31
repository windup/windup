package org.jboss.windup.rules.apps.tattletale;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that the Tattletale report should NOT be generated.
 *
 * @author <a href="mailto:marcorizzi82@gmail.com">Marco Rizzi</a>
 */
public class DisableTattletaleReportOption extends AbstractConfigurationOption {
    public static final String NAME = "disableTattletale";

    @Override
    public String getDescription() {
        return "If set, a Tattletale report won't be generated.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Should Windup avoid to generate a Tattletale report?";
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
    public ValidationResult validate(Object value) {
        return ValidationResult.SUCCESS;
    }

    @Override
    public Object getDefaultValue() {
        return false;
    }

}
