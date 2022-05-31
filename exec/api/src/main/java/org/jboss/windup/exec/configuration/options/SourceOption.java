package org.jboss.windup.exec.configuration.options;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Specifies the source framework or server to migrate from. This could include multiple items.
 * <p>
 * For example, this might be a migration from WebLogic, EJB2, and JSF 1.x to JBoss EAP 6, EJB 3, and JSF 2. Source in that case would include
 * WebLogic, EJB2, and JSF 1.x.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SourceOption extends AbstractConfigurationOption {
    public static final String NAME = "source";

    @Inject
    private RuleProviderRegistryCache cache;

    @Override
    public Collection<?> getAvailableValues() {
        return cache.getAvailableSourceTechnologies();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Source server or framework";
    }

    @Override
    public String getDescription() {
        return "The source server/technology/framework to migrate from. This could include multiple items (eg, \"eap\" and \"spring\") separated by a space. Use --listSourceTechnologies to get known sources.";
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
    public ValidationResult validate(Object values) {
        if (values != null) {
            for (Object value : (Iterable<?>) values) {
                if (value instanceof String && ((String) value).contains(":"))
                    value = StringUtils.substringBefore((String) value, ":");

                if (!getAvailableValues().contains(value))
                    return new ValidationResult(ValidationResult.Level.ERROR,
                            NAME + " value (" + value + ") not found, must be one of: " + getAvailableValues());
            }
        }

        return ValidationResult.SUCCESS;
    }
}
