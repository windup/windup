package org.jboss.windup.exec.configuration.options;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;

/**
 * Specifies the target framework or server to migrate to. This could include multiple items.
 * <p>
 * For example, this might be a migration from WebLogic, EJB2, and JSF 1.x to JBoss EAP 6, EJB 3, and JSF 2. Target in that case would include JBoss
 * EAP 6, EJB 3, and JSF 2.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TargetOption extends AbstractConfigurationOption {
    public static final String NAME = "target";

    @Inject
    private RuleProviderRegistryCache cache;

    @Override
    public Collection<?> getAvailableValues() {
        return cache.getAvailableTargetTechnologies();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Target server or framework";
    }

    @Override
    public String getDescription()
    {
        return "The target server/technology/framework to migrate to. This could include multiple items (eg, \"eap8\" and \"cloud-readiness\") separated by a space. Use --listTargetTechnologies to get known targets.";
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
        return true;
    }

    @Override
    public ValidationResult validate(Object values) {
        if (values == null) {
            return new ValidationResult(ValidationResult.Level.ERROR, NAME + " parameter is required!");
        }

        for (Object value : (Iterable<?>) values) {
            if (value instanceof String && ((String) value).contains(":"))
                value = StringUtils.substringBefore((String) value, ":");

            if (!getAvailableValues().contains(value))
                return new ValidationResult(ValidationResult.Level.ERROR,
                        NAME + " value (" + value + ") not found, must be one of: " + getAvailableValues());
        }

        return ValidationResult.SUCCESS;
    }

}
