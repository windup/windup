package org.jboss.windup.reporting.freemarker;

import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateMethodModelEx;
import org.apache.commons.lang3.StringUtils;

/**
 * This interface provides us with a way of looking up all TemplateMethodModel implementations within various windup-related addons.
 * <p>
 * This makes it possible for windup-addons to provide extension methods that can be easily accessed by the Freemarker templates.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupFreeMarkerMethod extends TemplateMethodModelEx {
    /**
     * Returns the name to be used for the function inside of Freemarker.
     */
    default String getMethodName() {
        return StringUtils.uncapitalize(StringUtils.removeEnd(this.getClass().getSimpleName(), "Method"));
    }

    /**
     * This should return a description of what this method does, along with any required parameters.
     */
    String getDescription();

    /**
     * Sets the current {@link GraphRewrite} event for {@link #exec(java.util.List)}.
     */
    default void setContext(GraphRewrite event) {
    }
}
