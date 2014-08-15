package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateDirectiveModel;

/**
 * This interface provides us with a way of looking up all TemplateDirectiveModel implementations within various
 * windup-related addons.
 * 
 * This makes it possible for windup-addons to provide extension methods that can be easily accessed by the freemarker
 * templates.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */

public interface WindupFreeMarkerTemplateDirective extends TemplateDirectiveModel
{

    /**
     * Returns the name to be used for the function inside of freemarker.
     */
    public String getDirectiveName();
}
