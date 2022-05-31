package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Specifies the tags to be included during Windup execution. The default behavior is to include all tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IncludeTagsOption extends AbstractConfigurationOption {
    public static final String NAME = "includeTags";

    @Inject
    private RuleProviderRegistryCache cache;

    @Override
    public Collection<?> getAvailableValues() {
        return cache.getAvailableTags();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Indicates the tags to process (by default all tags are processed)";
    }

    @Override
    public String getDescription() {
        return "Indicates the tags to process. If this is unset, then all tags will be processed. If this is set, then only Rules with the specified tags will be processed.";
    }

    @Override
    public InputType getUIType() {
        return InputType.SELECT_MANY;
    }

    @Override
    public Class<String> getType() {
        return String.class;
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
