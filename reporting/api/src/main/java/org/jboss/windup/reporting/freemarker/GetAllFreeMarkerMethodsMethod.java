package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.services.Imported;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns a list of all FreeMarker methods that have been added by Windup and the currently loaded addons.
 * <p>
 * Called as follows:
 * <p>
 * getAllFreeMarkerMethods()
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetAllFreeMarkerMethodsMethod implements WindupFreeMarkerMethod {
    @Inject
    private Imported<WindupFreeMarkerMethod> methods;

    @Override
    public String getDescription() {
        return "This method takes no parameters, and returns a List of hashes containing a 'name', 'description', and 'class' field.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        List<Map<String, String>> results = new ArrayList<>();
        for (WindupFreeMarkerMethod method : methods) {
            Map<String, String> methodInfo = new HashMap<>();
            methodInfo.put("name", method.getMethodName());
            methodInfo.put("description", method.getDescription());
            methodInfo.put("class", Proxies.unwrapProxyClassName(method.getClass()));
            results.add(methodInfo);
        }
        return results;
    }

}
