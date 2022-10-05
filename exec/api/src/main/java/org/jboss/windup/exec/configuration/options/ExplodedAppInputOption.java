package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that the input points to a directory with multiple applications rather than being an application itself.
 *
 * @author Ondrej Zizka
 */
public class ExplodedAppInputOption extends AbstractConfigurationOption {
    public static final String NAME = "explodedApp";

    @Override
    public String getDescription() {
        return "If the input path points to a directory, it should be treated as an unzipped application, "
                + "instead of a directory containing applications (default).";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Unzipped Applications Input";
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
    public Object getDefaultValue() {
        return Boolean.FALSE;
    }

}
