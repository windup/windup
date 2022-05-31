package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Specifies the tags to be excluded during Windup execution. The default behavior is not to exclude any tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ExcludeTagsOption extends AbstractConfigurationOption {
    public static final String NAME = "excludeTags";

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
        return "Indicates the tags to explicitly exclude from processing (by default all tags are processed). Multiple tags can be specified separated by a space (eg, --"
                + NAME + " TAG_1 TAG_@";
    }

    @Override
    public String getDescription() {
        return "Indicates the tags to exclude from processing. If this is unset, then all tags will be processed. If this is set, then Rules with the specified tags will be skipped.";
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
