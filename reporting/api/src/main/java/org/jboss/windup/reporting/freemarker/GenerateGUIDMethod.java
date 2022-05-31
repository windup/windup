package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.UUID;

/**
 * Generates a unique identifier as a String.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GenerateGUIDMethod implements WindupFreeMarkerMethod {
    @Override
    public String getDescription() {
        return "Generates a unique identifier as a String.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        return UUID.randomUUID().toString();
    }
}
