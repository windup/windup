package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.pegdown.WindupCodeBlockPlugin;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

/**
 * Converts from an input string in Markdown format to an output string in HTML.
 * 
 * @author jsightler
 */
public class MarkdownToHtmlMethod implements WindupFreeMarkerMethod
{

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (String)");
        }
        SimpleScalar freemarkerArg = (SimpleScalar) arguments.get(0);
        String markdownSource = freemarkerArg.getAsString();

        // build the plugins object with our extensions
        PegDownPlugins plugins = PegDownPlugins.builder().withPlugin(WindupCodeBlockPlugin.class).build();
        PegDownProcessor proc = new PegDownProcessor(Extensions.NONE, plugins);

        // build the node and then serialize it so that we can make sure the serializer uses our plugins
        RootNode outputNode = proc.parseMarkdown(markdownSource.toCharArray());

        // Out plugin is also a serializer, so build a plugins list for serialization as well
        List<ToHtmlSerializerPlugin> serializerPlugins = new ArrayList<>(1);
        serializerPlugins.add(new WindupCodeBlockPlugin());

        ToHtmlSerializer serializer = new ToHtmlSerializer(new LinkRenderer(), Collections.<String, VerbatimSerializer> emptyMap(), serializerPlugins);
        return serializer.toHtml(outputNode);
    }

    @Override
    public String getMethodName()
    {
        return "markdownToHtml";
    }

    @Override
    public String getDescription()
    {
        return "Converts from an input string in Markdown format to an output string in HTML format";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
    }

}
