package org.jboss.windup.config;

import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.util.ThemeProvider;

/**
 * Indicates whether Windup processing should render source code reports at all.
 *
 */
public class SkipSourceCodeReportsRenderingOption extends AbstractConfigurationOption {
    public static final String NAME = WindupConfigurationModel.SKIP_SOURCE_CODE_REPORTS_RENDERING;

    @Override
    public String getDescription() {
        return "If set, " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " will not generate source code reports.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Should " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " generate HTML source code reports?";
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

}
