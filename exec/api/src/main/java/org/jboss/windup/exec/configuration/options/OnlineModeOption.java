package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicate that network related actions are allowed (they should be disabled by default).
 */
public class OnlineModeOption extends AbstractConfigurationOption {
    public static final String NAME = "online";

    @Override
    public String getDescription() {
        return "Indicates that network access is allowed. By turning it on, XML " +
                "schemas can be validated against external resources, however, this " +
                "comes with a performance penalty.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Online Mode";
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
    public ValidationResult validate(Object valueObj) {
        return ValidationResult.SUCCESS;
    }
}
