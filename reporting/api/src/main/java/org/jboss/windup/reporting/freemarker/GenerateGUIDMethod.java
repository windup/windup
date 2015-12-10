package org.jboss.windup.reporting.freemarker;

import java.util.List;
import java.util.UUID;

import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateModelException;

/**
 * Generates a unique identifier as a String.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GenerateGUIDMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "generateGUID";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Generates a unique identifier as a String.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        return UUID.randomUUID().toString();
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }
}
