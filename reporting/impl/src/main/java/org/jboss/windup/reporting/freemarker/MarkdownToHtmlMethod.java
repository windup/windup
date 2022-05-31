package org.jboss.windup.reporting.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.Logging;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts from an input string in Markdown format to an output string in HTML.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class MarkdownToHtmlMethod implements WindupFreeMarkerMethod {
    public static final long MAX_PARSING_TIME_MILLIS = 100000;
    private static final Logger LOG = Logging.get(MarkdownToHtmlMethod.class);
    private static final Map<String, SoftReference<String>> cache = new ConcurrentHashMap<>();

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (String)");
        }
        SimpleScalar freemarkerArg = (SimpleScalar) arguments.get(0);
        String markdownSource = freemarkerArg.getAsString();

        SoftReference<String> cachedResult = cache.get(markdownSource);
        String cachedResultString;
        if (cachedResult != null && (cachedResultString = cachedResult.get()) != null) {
            return cachedResultString;
        }

        try {
            // build the plugins object with our extensions
            PegDownPlugins plugins = PegDownPlugins.builder().build();
            PegDownProcessor processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS, MAX_PARSING_TIME_MILLIS, plugins);

            // build the node and then serialize it so that we can make sure the serializer uses our plugins
            RootNode outputNode = processor.parseMarkdown(markdownSource.toCharArray());

            // Our plugin is also a serializer, so build a plugins list for serialization as well
            List<ToHtmlSerializerPlugin> serializerPlugins = new ArrayList<>(1);

            ToHtmlSerializer serializer = new ToHtmlSerializerExtended(new LinkRenderer(), Collections.<String, VerbatimSerializer>emptyMap(),
                    serializerPlugins);
            String result = serializer.toHtml(outputNode);
            cache.put(markdownSource, new SoftReference<>(result));
            return result;
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Failed to parse markdown due to: " + t.getMessage() + " markdown source: " + markdownSource, t);
            // Return the unformatted markdown, as this is better than failing the report completely.
            return markdownSource;
        }
    }

    @Override
    public String getMethodName() {
        return "markdownToHtml";
    }

    @Override
    public String getDescription() {
        return "Converts from an input string in Markdown format to an output string in HTML format";
    }

    @Override
    public void setContext(GraphRewrite event) {
    }
}
