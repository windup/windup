package org.jboss.windup.reporting.freemarker;

import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateMethodModelEx;

/**
 * This interface provides us with a way of looking up all TemplateMethodModel implementations within various windup-related addons.
 * 
 * This makes it possible for windup-addons to provide extension methods that can be easily accessed by the freemarker templates.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public interface WindupFreeMarkerMethod extends TemplateMethodModelEx
{
    /**
     * Returns the name to be used for the function inside of freemarker.
     */
    String getMethodName();

    /**
     * This should return a description of what this method does, along with any required parameters.
     */
    String getDescription();

    /**
     * Sets the current {@link GraphRewrite} event for {@link #exec(java.util.List)}.
     */
    void setContext(GraphRewrite event);
}
