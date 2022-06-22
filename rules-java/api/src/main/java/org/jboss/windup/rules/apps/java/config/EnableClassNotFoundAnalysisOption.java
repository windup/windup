package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Gives the user the option to skip class not found analysis.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EnableClassNotFoundAnalysisOption extends AbstractConfigurationOption {
    public static final String NAME = "enableClassNotFoundAnalysis";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Enable Class Not Found Analysis";
    }

    @Override
    public String getDescription() {
        return "Enable analysis of Java files that are not available on the Classpath. This should be left off if" +
                " some classes will be unavailable at analysis time.";
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
