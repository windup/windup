package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

public class ExportSummaryOption extends AbstractConfigurationOption {
    public static final String NAME = "exportSummary";

    @Override
    public String getDescription() {
        return "Indicates whether to export a data file containing a summary of the analysis results.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Export Summary";
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
