package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class InputApplicationName extends AbstractConfigurationOption {
    public static final String NAME = "inputApplicationName";

    @Override
    public int getPriority() {
        return -1000;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Application Name";
    }

    @Override
    public String getDescription() {
        return "This represents the full name of a given application.";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public InputType getUIType() {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public ValidationResult validate(Object value) {
        return ValidationResult.SUCCESS;
    }
}
