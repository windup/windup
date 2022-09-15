package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that the input is source code (as opposed to compiled code).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SourceModeOption extends AbstractConfigurationOption {
    public static final String NAME = "sourceMode";

    @Override
    public String getDescription() {
        return "Indicates whether the input file or directory is a source code or compiled binaries (Default: Compiled).";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Source Mode";
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

    public ValidationResult validate(Object valueObj) {
        return ValidationResult.SUCCESS;
    }
}
