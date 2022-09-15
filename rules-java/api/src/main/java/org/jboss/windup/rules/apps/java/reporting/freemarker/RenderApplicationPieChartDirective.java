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
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
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
import org.jboss.windup.util.exception.WindupException;

/**
 * Renders a JavaScript block that calls <a href="http://www.flotcharts.org/">Flot</a>. This depends upon the template already loading the JQuery and
 * Flot Charting Javascript files.
 * <p>
 * The chart will present a distribution of packages that have been hinted by Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RenderApplicationPieChartDirective implements WindupFreeMarkerTemplateDirective {
    public static final String NAME = "render_pie";

    private GraphContext context;

    @Override
    public String getDescription() {
        return "Renders a pie chart. Takes the following parameters: project (a " + ProjectModel.class.getSimpleName()
                + "), recursive (Boolean), and elementid (HTML ID of the element in which to render).";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body)
            throws TemplateException, IOException {
        StringModel projectStringModel = (StringModel) params.get("project");
        ProjectModel projectModel = null;
        if (projectStringModel != null)
            projectModel = (ProjectModel) projectStringModel.getWrappedObject();

        StringModel projectTraversalStringModel = (StringModel) params.get("projectTraversal");
        ProjectModelTraversal projectTraversal = null;
        if (projectTraversalStringModel != null)
            projectTraversal = (ProjectModelTraversal) projectTraversalStringModel.getWrappedObject();

        if (projectModel != null && projectTraversal != null)
            throw new WindupException(NAME + " both 'project' and 'projectTraversal' were specified. Only one of these values should be specified.");
        else if (projectModel == null && projectTraversal == null)
            throw new WindupException(NAME + " neither 'project' nor 'projectTraversal' were specified. At least one of these must be specified.");

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) params.get("recursive");
        boolean recursive = recursiveBooleanModel.getAsBoolean();
        SimpleScalar elementIDStringModel = (SimpleScalar) params.get("elementID");
        String elementID = elementIDStringModel.getAsString();

        Set<String> includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) params.get("includeTags"));
        Set<String> excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) params.get("excludeTags"));

        TypeReferenceService typeReferenceService = new TypeReferenceService(context);
        Map<String, Integer> data;
        if (projectModel != null)
            data = typeReferenceService.getPackageUseFrequencies(projectModel, includeTags, excludeTags, 2, recursive);
        else
            data = typeReferenceService.getPackageUseFrequencies(projectTraversal, includeTags, excludeTags, 2, recursive);

        if (data.keySet().size() > 0) {
            drawPie(env.getOut(), data, elementID);
        } else {
            // if we aren't drawing a pie, remove the element that would have held it
            Writer writer = env.getOut();
            writer.append("<script type='text/javascript'>");
            writer.append("$('#" + elementID + "').parent().remove()");
            writer.append("</script>");
        }
    }

    private void drawPie(Writer writer, Map<String, Integer> data, String elementID) throws IOException {
        List<PieSort> pieList = topX(data, 9);

        String dataVarName = "data_" + elementID;
        writer.append("<script type='text/javascript'>");
        writer.append(System.lineSeparator() + " WINDUP_PACKAGE_PIE_DATA = typeof(WINDUP_PACKAGE_PIE_DATA) == 'undefined' ? {} : WINDUP_PACKAGE_PIE_DATA;");
        writer.append(System.lineSeparator() + " WINDUP_PACKAGE_PIE_DATA['").append(elementID).append("'] = [];");
        for (PieSort p : pieList) {
            writer.append(System.lineSeparator() + " WINDUP_PACKAGE_PIE_DATA['").append(elementID).append("'].push({label: '" + p.label + "', data: ")
                    .append(p.value.toString()).append("});");
        }

        writer.append(System.lineSeparator() + " $(function () {");
        writer.append(System.lineSeparator() + "   var " + dataVarName + " = [];");
        for (PieSort p : pieList)
            writer.append(System.lineSeparator() + " ").append(dataVarName).append(".push({ label: '").append(p.key).append("', data: ").append(p.value.toString()).append(" });");

        writer.append(System.lineSeparator() + "   $.plot($('#" + elementID + "'), " + dataVarName + ", {");
        writer.append(System.lineSeparator() + "       series: { pie: { show: true,  innerRadius: 0.55, offset: { top: 0, left: -120 } } },");
        writer.append(System.lineSeparator() + "       colors: $.map( " + dataVarName + ", function(item, index) {" +
                System.lineSeparator() + "           var len = " + dataVarName + ".length;" +
                System.lineSeparator() + "           return jQuery.Color({" +
                System.lineSeparator() + "               hue: ((index*0.95*360/len) + 90/len) % 360," +
                System.lineSeparator() + "               saturation: 0.95," +
                System.lineSeparator() + "               lightness: ((index%4 == 3 ? 1:0)/-4)+0.55, alpha: 1" +
                System.lineSeparator() + "           }).toHexString();" +
                System.lineSeparator() + "       })");
        writer.append(System.lineSeparator() + "   });");
        writer.append(System.lineSeparator() + " });");
        writer.append("</script>");
    }

    @Override
    public String getDirectiveName() {
        return NAME;
    }

    private List<PieSort> topX(Map<String, Integer> map, int top) {
        List<PieSort> list = new ArrayList<>(map.keySet().size() + 1);

        // Add the key/value pairs to the list containing the PieSort(key,value) object.
        for (String key : map.keySet()) {
            PieSort p = new PieSort(key + " - " + map.get(key) + "&times;", key, map.get(key));
            list.add(p);
        }

        Collections.sort(list);

        // Collect the bottom of the list for the "Other" category.
        if (top < list.size()) {
            // Add the "Other" category up.
            int other = 0;
            for (PieSort p : list.subList(top, list.size()))
                other += p.value;

            list = list.subList(0, top);
            if (other > 0)
                list.add(new PieSort("Other - " + other + "&times;", "Other", other));
        }

        return list;
    }

    private static class PieSort implements Comparable<PieSort> {
        public final String key;
        public final String label;
        public final Integer value;

        PieSort(String k, String label, Integer v) {
            this.key = k;
            this.label = label;
            this.value = v;
        }

        @Override
        public int compareTo(PieSort p) {
            return p.value - value;
        }
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }

}
