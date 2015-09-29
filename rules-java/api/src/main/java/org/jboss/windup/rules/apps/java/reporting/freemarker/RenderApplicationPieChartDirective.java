package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.FreeMarkerUtil;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * Renders a JavaScript block that calls <a href="http://www.flotcharts.org/">Flot</a>. This depends upon the template already loading the JQuery and
 * Flot Charting Javascript files.
 * 
 * The chart will present a distribution of packages that have been hinted by Windup.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class RenderApplicationPieChartDirective implements WindupFreeMarkerTemplateDirective
{
    private GraphContext context;

    @Override
    public String getDescription()
    {
        return "Renders a pie chart. Takes the following parameters: project (a " + ProjectModel.class.getSimpleName()
                    + "), recursive (Boolean), and elementid (HTML ID of the element in which to render).";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
                TemplateDirectiveBody body)
                throws TemplateException, IOException
    {
        StringModel projectStringModel = (StringModel) params.get("project");
        ProjectModel projectModel = (ProjectModel) projectStringModel.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) params.get("recursive");
        boolean recursive = recursiveBooleanModel.getAsBoolean();
        SimpleScalar elementIDStringModel = (SimpleScalar) params.get("elementID");
        String elementID = elementIDStringModel.getAsString();

        Set<String> includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) params.get("includeTags"));
        Set<String> excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) params.get("excludeTags"));

        TypeReferenceService typeReferenceService = new TypeReferenceService(context);
        Map<String, Integer> data = typeReferenceService.getPackageUseFrequencies(projectModel, includeTags, excludeTags, 2, recursive);
        if (data.keySet().size() > 0)
        {
            drawPie(env.getOut(), data, elementID);
        }
        else
        {
            // if we aren't drawing a pie, remove the element that would have held it
            Writer writer = env.getOut();
            writer.append("<script type='text/javascript'>");
            writer.append("$('#" + elementID + "').parent().remove()");
            writer.append("</script>");
        }
    }

    private void drawPie(Writer writer, Map<String, Integer> data, String elementID) throws IOException
    {
        List<PieSort> pieList = topX(data, 9);

        String dataID = "data_" + elementID;
        writer.append("<script type='text/javascript'>");
        writer.append("\n").append("$(function () {");
        writer.append("\n").append("  var " + dataID + " = [];");

        int i = 0;
        for (PieSort p : pieList)
        {
            writer.append("\n").append(
                        dataID + "[" + i + "] = { label: '" + p.key + "', data: " + p.value + " };");
            i++;
        }
        writer.append("\n").append("  $.plot($('#" + elementID + "'), " + dataID + ", {");
        writer.append("\n").append("      series: {");
        writer.append("\n").append("          pie: {");
        writer.append("\n").append("              show: true");
        writer.append("\n").append("          }");
        writer.append("\n").append("      }");
        writer.append("\n").append("  });");
        writer.append("\n").append("});");
        writer.append("</script>");
    }

    @Override
    public String getDirectiveName()
    {
        return "render_pie";
    }

    private List<PieSort> topX(Map<String, Integer> map, int top)
    {
        List<PieSort> list = new ArrayList<PieSort>(map.keySet().size() + 1);
        List<PieSort> bottomList;

        // Add the key/value pairs to the list containing the PieSort(key,value) object.
        for (String key : map.keySet())
        {
            PieSort p = new PieSort(key + " - " + map.get(key), map.get(key));
            list.add(p);
        }

        Collections.sort(list);

        // Collect the bottom of the list for the "Other" category.
        int other = 0;
        if (top < list.size())
        {
            bottomList = list.subList(top, list.size());

            // Add the "Other" category up.
            for (PieSort p : bottomList)
            {
                other += p.value;
            }

            list = list.subList(0, top);
        }

        if (other > 0)
        {
            list.add(new PieSort("Other" + " - " + other, other));
        }

        return list;
    }

    private class PieSort implements Comparable<PieSort>
    {
        public String key;
        public Integer value;

        public PieSort(String k, Integer v)
        {
            this.key = k;
            this.value = v;
        }

        @Override
        public int compareTo(PieSort p)
        {
            if (value < p.value)
                return 1;
            if (value == p.value)
                return 0;
            else
                return -1;
        }
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

}
