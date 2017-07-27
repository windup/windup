package org.jboss.windup.ui;

import java.util.concurrent.Callable;

import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.util.Util;

/**
 * An adapter between {@link ConfigurationOption#getDefaultValue()} default values, and
 * {@link UIInput#setDefaultValue(Callable)}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DefaultValueAdapter<T> implements Callable<T>
{
    private ConfigurationOption option;
    private Class<T> expectedType;

    public DefaultValueAdapter(ConfigurationOption option, Class<T> expectedType)
    {
        this.option = option;
        this.expectedType = expectedType;
    }

    public DefaultValueAdapter(ConfigurationOption option)
    {
        this(option, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T call() throws Exception
    {
        Object val = this.option.getDefaultValue();
        if (val != null && this.expectedType != null && !this.expectedType.isAssignableFrom(val.getClass()))
        {
            throw new IllegalStateException(Util.WINDUP_BRAND_NAME_ACRONYM + " option " + option.getName() +
                        " was expected to return " + expectedType.getName() + " but returned " + val.getClass());
        }
        return (T) val;
    }
}