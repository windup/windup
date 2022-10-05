package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AdditionalClasspathOption extends AbstractPathConfigurationOption {
    public static final String NAME = "additionalClasspath";

    public AdditionalClasspathOption() {
        super(true);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Additional Classpath";
    }

    @Override
    public String getDescription() {
        return "Adds additional files or directories to the classpath";
    }

    @Override
    public InputType getUIType() {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
