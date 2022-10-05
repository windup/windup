package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that windup should export the report into the csv file.
 */
public class ExportCSVOption extends AbstractConfigurationOption {
    public static final String NAME = "exportCSV";

    @Override
    public String getDescription() {
        return "Indicates whether to export CSV file containing the migration information.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Export CSV";
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
