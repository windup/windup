package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;
import java.util.List;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GetWindupBrandNameMethod implements WindupFreeMarkerMethod {
    @Override
    public String getMethodName()
    {
        return "getWindupBrandName";
    }

    @Override
    public String getDescription()
    {
        return "Returns the name to use in place of the project name.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        return org.jboss.windup.util.Util.WINDUP_BRAND_NAME_LONG;
    }
}
