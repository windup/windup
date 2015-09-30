package org.jboss.windup.reporting.freemarker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.WindupVertexListModel;
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
     * Converts a FreeMarker {@link SimpleSequence} to a {@link Set}.
     *
     */
    public static Set<String> simpleSequenceToSet(SimpleSequence simpleSequence)
    {
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
        Map<String, Object> results = new HashMap<String, Object>();

        for (WindupFreeMarkerMethod freeMarkerMethod : freeMarkerMethods)
        {
            freeMarkerMethod.setContext(event);
            if (results.containsKey(freeMarkerMethod.getMethodName()))
            {
                throw new WindupException("Windup contains two freemarker extension providing the same name: "
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
                throw new WindupException("Windup contains two freemarker extension providing the same name: "
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
        Map<String, Object> results = new HashMap<String, Object>();

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

    /**
     * Add report data as associations to this report instance
     */
    @SuppressWarnings("unchecked")
    public static void addAssociatedReportData(GraphContext context, ReportModel reportModel,
                Map<String, Object> reportData)
    {
        Map<String, WindupVertexFrame> relatedResources = new HashMap<>();
        for (Map.Entry<String, Object> varEntry : reportData.entrySet())
        {
            Object value = varEntry.getValue();
            if (value instanceof WindupVertexFrame)
            {
                relatedResources.put(varEntry.getKey(), (WindupVertexFrame) varEntry.getValue());
            }
            else if (value instanceof Iterable)
            {
                WindupVertexListModel list = context.getFramed().addVertex(null, WindupVertexListModel.class);
                for (WindupVertexFrame frame : (Iterable<? extends WindupVertexFrame>) value)
                {
                    list.addItem(frame);
                }
                relatedResources.put(varEntry.getKey(), list);
            }
            else
            {
                throw new WindupException("Unrecognized variable type: " + value.getClass().getCanonicalName()
                            + " encountered!");
            }
        }
    }
}
