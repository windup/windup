package org.jboss.windup.config;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides a base class for sharing default functionality between {@link ConfigurationOption}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public abstract class AbstractConfigurationOption implements ConfigurationOption {
    private Collection<?> availableValues = Collections.emptyList();

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Collection<?> getAvailableValues() {
        return availableValues;
    }

    protected void setAvailableValues(Collection<?> availableValues) {
        this.availableValues = availableValues;
    }

    @Override
    public Object getDefaultValue() {
        if (Boolean.class.isAssignableFrom(this.getType()) || boolean.class.isAssignableFrom(this.getType())) {
            return false;
        } else
            return null;
    }
}
