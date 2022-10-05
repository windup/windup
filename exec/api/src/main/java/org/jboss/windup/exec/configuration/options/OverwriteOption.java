package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that Windup should overwrite the output directory if it already exists (without prompting).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class OverwriteOption extends AbstractConfigurationOption {
    public static final String NAME = "overwrite";

    @Override
    public String getDescription() {
        return "Force overwrite of the output directory, without prompting.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Overwrite";
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

    @Override
    public int getPriority() {
        return 8500;
    }
}
