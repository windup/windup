package org.jboss.windup.reporting.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.rules.rendering.RenderRuleProviderReportRuleProvider;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RenderRuleLinkDirective implements WindupFreeMarkerTemplateDirective
{

    public static final String RENDER_RULE_LINK = "render_rule_link";
    public static final String RENDER_TYPE_GLYPH = "glyph";

    @Override
    public String getDirectiveName()
    {
        return RENDER_RULE_LINK;
    }

    @Override
    public String getDescription()
    {
        return "Renders a link to the Rule with the specified ID in the Rule Provider report.";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        // no-op
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException
    {
        final Writer writer = env.getOut();
        SimpleScalar ruleIDStringModel = (SimpleScalar) params.get("ruleID");
        if (ruleIDStringModel == null || ruleIDStringModel.getAsString() == null)
            return;

        SimpleScalar renderTypeScalar = (SimpleScalar) params.get("renderType");
        final String renderType;
        if (renderTypeScalar == null)
            renderType = RENDER_TYPE_GLYPH;
        else
            renderType = renderTypeScalar.getAsString();

        String ruleID = ruleIDStringModel.getAsString();

        writer.append("<a href='" + RenderRuleProviderReportRuleProvider.OUTPUT_FILENAME + "#" + ruleID + "'>");
        if (RENDER_TYPE_GLYPH.equals(renderType))
            writer.append("<span title='" + ruleID + "' class='glyphicon glyphicon-link'></span>");
        else
            writer.append("<span title='" + ruleID + "'>" + ruleID + "</span>");
        writer.append("</a>");
    }
}
