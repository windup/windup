package org.jboss.windup.rules.apps.java.reporting;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.rules.apps.java.service.JavaInlineHintService;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class RenderApplicationPieChartDirective implements WindupFreeMarkerTemplateDirective
{

    @Inject
    private JavaInlineHintService javaInlineHintService;

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

        Map<String, Integer> data = javaInlineHintService.getPackageUseFrequencies(projectModel, 3, recursive);
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

}
