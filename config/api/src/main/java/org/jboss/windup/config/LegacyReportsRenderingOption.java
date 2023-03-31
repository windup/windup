package org.jboss.windup.config;

import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.util.ThemeProvider;

/**
 * Indicates whether Windup processing should render legacy reports, otherwise new Patternfly 4 reports will be rendered.
 */
public class LegacyReportsRenderingOption extends AbstractConfigurationOption {
    public static final String NAME = WindupConfigurationModel.LEGACY_REPORTS;

    @Override
    public String getDescription() {
        return "If set, " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " generates legacy reports.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Should " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " generate legacy reports?";
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
