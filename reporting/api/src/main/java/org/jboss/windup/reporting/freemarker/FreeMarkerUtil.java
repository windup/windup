package org.jboss.windup.reporting.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;

/**
 * This class contains several useful utility functions that can be used for rendering a freemarker template within Windup.
 *
 */
public class FreeMarkerUtil
{
    /**
     * Gets the default configuration for Freemarker within Windup.
     */
    public static Configuration getDefaultFreemarkerConfiguration()
    {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        DefaultObjectWrapperBuilder objectWrapperBuilder = new DefaultObjectWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        objectWrapperBuilder.setUseAdaptersForContainers(true);
        configuration.setObjectWrapper(objectWrapperBuilder.build());
        configuration.setAPIBuiltinEnabled(true);

        configuration.setTemplateLoader(new FurnaceFreeMarkerTemplateLoader());
        configuration.setTemplateUpdateDelayMilliseconds(3600);
        return configuration;
    }

    /**
     * Converts a FreeMarker {@link SimpleSequence} to a {@link Set}.
     *
     */
    public static Set<String> simpleSequenceToSet(SimpleSequence simpleSequence)
    {
        if (simpleSequence == null)
            return Collections.emptySet();

        Set<String> result = new HashSet<>();
        for (int i = 0; i < simpleSequence.size(); i++)
        {
            try
            {
                Object sequenceEntry = simpleSequence.get(i);
                if (sequenceEntry instanceof SimpleScalar)
                {
                    result.add(((SimpleScalar) sequenceEntry).getAsString());
                }
                else
                {
                    result.add(simpleSequence.get(i).toString());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * Gets freemarker extensions (eg, custom functions) provided by furnace addons
     */
    public static Map<String, Object> findFreeMarkerExtensions(Furnace furnace, GraphRewrite event)
    {
        Imported<WindupFreeMarkerMethod> freeMarkerMethods = furnace.getAddonRegistry().getServices(
                    WindupFreeMarkerMethod.class);
        Map<String, Object> results = new HashMap<>();

        for (WindupFreeMarkerMethod freeMarkerMethod : freeMarkerMethods)
        {
            freeMarkerMethod.setContext(event);
            if (results.containsKey(freeMarkerMethod.getMethodName()))
            {
                throw new WindupException(Util.WINDUP_BRAND_NAME_ACRONYM+" contains two freemarker extension providing the same name: "
                            + freeMarkerMethod.getMethodName());
            }
            results.put(freeMarkerMethod.getMethodName(), freeMarkerMethod);
        }

        Imported<WindupFreeMarkerTemplateDirective> freeMarkerDirectives = furnace.getAddonRegistry().getServices(
                    WindupFreeMarkerTemplateDirective.class);
        for (WindupFreeMarkerTemplateDirective freeMarkerDirective : freeMarkerDirectives)
        {
            freeMarkerDirective.setContext(event);
            if (results.containsKey(freeMarkerDirective.getDirectiveName()))
            {
                throw new WindupException(Util.WINDUP_BRAND_NAME_ACRONYM+" contains two freemarker extension providing the same name: "
                            + freeMarkerDirective.getDirectiveName());
            }
            results.put(freeMarkerDirective.getDirectiveName(), freeMarkerDirective);
        }

        return results;
    }

    /**
     * Finds all variables in the context with the given names, and also attaches all WindupFreeMarkerMethods from all addons into the map.
     *
     * This allows external addons to extend the capabilities in the freemarker reporting system.
     */
    public static Map<String, Object> findFreeMarkerContextVariables(Variables variables, String... varNames)
    {
        Map<String, Object> results = new HashMap<>();

        for (String varName : varNames)
        {
            WindupVertexFrame payload = null;
            try
            {
                payload = Iteration.getCurrentPayload(variables, null, varName);
            }
            catch (IllegalStateException | IllegalArgumentException e)
            {
                // oh well
            }

            if (payload != null)
            {
                results.put(varName, payload);
            }
            else
            {
                Iterable<? extends WindupVertexFrame> var = variables.findVariable(varName);
                if (var != null)
                {
                    results.put(varName, var);
                }
            }
        }
        return results;
    }
}
