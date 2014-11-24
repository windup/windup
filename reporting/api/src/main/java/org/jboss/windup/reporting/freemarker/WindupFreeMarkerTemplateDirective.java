package org.jboss.windup.reporting.freemarker;

import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateDirectiveModel;

/**
 * This interface provides us with a way of looking up all TemplateDirectiveModel implementations within various windup-related addons.
 * 
 * This makes it possible for windup-addons to provide extension methods that can be easily accessed by the freemarker templates.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */

public interface WindupFreeMarkerTemplateDirective extends TemplateDirectiveModel
{
    /**
     * Returns the name to be used for the function inside of freemarker.
     */
    public String getDirectiveName();

    /**
     * This should return a description of what this directive does, along with any required parameters.
     */
    String getDescription();

    /**
     * Set the current {@link GraphRewrite} event to use for
     * {@link #execute(freemarker.core.Environment, java.util.Map, freemarker.template.TemplateModel[], freemarker.template.TemplateDirectiveBody)}
     */
    public void setContext(GraphRewrite event);
}
