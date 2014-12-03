package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateModelException;

/**
 * 
 * Returns a list of all FreeMarker Template Directives that have been added by Windup and the currently loaded addons.
 * 
 * Called as follows:
 * 
 * getAllFreeMarkerDirectives()
 * 
 * @author jsightler
 *
 */
public class GetAllFreeMarkerDirectivesMethod implements WindupFreeMarkerMethod
{
    @Inject
    private Imported<WindupFreeMarkerTemplateDirective> directives;

    @Override
    public String getMethodName()
    {
        return "getAllFreeMarkerDirectives";
    }

    @Override
    public String getDescription()
    {
        return "This method takes no parameters, and returns a List of hashes containing a 'name', 'class',  and 'description' field.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        List<Map<String, String>> results = new ArrayList<>();
        for (WindupFreeMarkerTemplateDirective directive : directives)
        {
            Map<String, String> directiveInfo = new HashMap<>();
            directiveInfo.put("name", directive.getDirectiveName());
            directiveInfo.put("description", directive.getDescription());
            directiveInfo.put("class", Proxies.unwrapProxyClassName(directive.getClass()));
            results.add(directiveInfo);
        }
        return results;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
    }
}
