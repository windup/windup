package org.jboss.windup.config;

import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.util.ThemeProvider;

/**
 * Indicates output path will be compressed in a ZIP file
 *
 * @author Marco Rizzi
 */
public class ExportZipOutputOption extends AbstractConfigurationOption {
    public static final String NAME = WindupConfigurationModel.EXPORT_ZIP_REPORT;

    @Override
    public String getDescription() {
        return "If set, " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " will create a ZIP file containing the files from the output path.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Should " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " create a ZIP file with the file in the output path?";
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
