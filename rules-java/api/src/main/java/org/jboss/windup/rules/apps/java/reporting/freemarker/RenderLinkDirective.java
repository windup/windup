package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.Logging;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.jboss.windup.util.IterableConverter;

/**
 * Renders linkable elements as a list of links.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class RenderLinkDirective implements WindupFreeMarkerTemplateDirective
{
    private static final Logger LOG = Logging.get(RenderLinkDirective.class);
    public static final String NAME = "render_link";
    private GraphContext context;
    private SourceReportService sourceReportService;
    private JavaClassService javaClassService;

    @Override
    public String getDescription()
    {
        return "Takes the following parameters: FileModel (a " + FileModel.class.getSimpleName() + ")";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
                throws TemplateException, IOException
    {
        final Writer writer = env.getOut();
        StringModel stringModel = (StringModel) params.get("model");

        SimpleScalar defaultTextModel = (SimpleScalar) params.get("text");
        String defaultText = params.get("text") == null ? null : defaultTextModel.getAsString();

        if (stringModel == null || stringModel.getWrappedObject() == null)
        {
            if (StringUtils.isNotBlank(defaultText))
                writer.append(defaultText);
            else
            {
                writer.append("unknown");
                LOG.warning("Failed to resolve name or text for " + getClass().getName() + ". " + env);
            }
            return;
        }

        Object model = stringModel.getWrappedObject();

        LayoutType layoutType = resolveLayoutType(params);
        String cssClass = resolveCssClass(params);

        if (model instanceof FileLocationModel)
        {
            processFileLocationModel(writer, cssClass, (FileLocationModel) model, defaultText);
        }
        else if (model instanceof FileModel)
        {
            processFileModel(writer, cssClass, (FileModel) model, defaultText);
        }
        else if (model instanceof JavaClassModel)
        {
            processJavaClassModel(writer, cssClass, (JavaClassModel) model, defaultText);
        }
        else if (model instanceof LinkableModel)
        {
            processLinkableModel(writer, layoutType, cssClass, (LinkableModel) model, defaultText);
        }
        else
        {
            throw new TemplateException("Object type not permitted: " + model.getClass().getSimpleName(), env);
        }
    }

    private String resolveCssClass(Map params)
    {
        SimpleScalar css = (SimpleScalar) params.get("class");
        if (css == null)
            return "";
        else
            return css.getAsString();
    }

    private LayoutType resolveLayoutType(Map params) throws TemplateException
    {
        LayoutType layoutType = LayoutType.HORIZONTAL;
        SimpleScalar layoutModel = (SimpleScalar) params.get("layout");
        if (layoutModel != null)
        {
            String lt = layoutModel.getAsString();
            try
            {
                layoutType = LayoutType.valueOf(lt.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new TemplateException("Layout: " + lt + " is not supported.", e, null);
            }
        }
        return layoutType;
    }

    private void processFileLocationModel(Writer writer, String cssClass, FileLocationModel obj, String defaultText) throws IOException
    {
        String position = " (" + obj.getLineNumber() + ", " + obj.getColumnNumber() + ")";
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(obj.getFile()) + position : defaultText;
        String anchor = obj.asVertex().getId().toString();

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(obj.getFile());
        if (result == null)
            writer.write(linkText);
        else
            renderLink(writer, cssClass, result.getReportFilename() + "#" + anchor, linkText);
    }

    private void processLinkableModel(Writer writer, LayoutType layoutType, String cssClass, LinkableModel obj, String defaultText) throws IOException
    {
        Iterable<Link> links = new IterableConverter<LinkModel, Link>(obj.getLinks())
        {
            public Link from(LinkModel m) { return new Link(m.getLink(), m.getDescription()); }
        };
        renderLinks(writer, layoutType, links);
    }

    private void processFileModel(Writer writer, String cssClass, FileModel fileModel, String defaultText) throws IOException
    {
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(fileModel) : defaultText;

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(fileModel);
        if (result == null)
            writer.write(linkText);
        else
            renderLink(writer, cssClass, result.getReportFilename(), linkText);
    }

    private void processJavaClassModel(Writer writer, String cssClass, JavaClassModel clz, String defaultText)
                throws IOException
    {
        Iterator<JavaSourceFileModel> results = javaClassService.getJavaSource(clz.getQualifiedName()).iterator();

        if (!results.hasNext())
        {
            writer.write(clz.getQualifiedName());
            return;
        }

        String linkText = StringUtils.isBlank(defaultText) ? clz.getQualifiedName() : defaultText;
        int i = 2;
        while (results.hasNext())
        {
            JavaSourceFileModel source = results.next();
            SourceReportModel result = sourceReportService.getSourceReportForFileModel(source);
            if (result == null)
                writer.write(linkText);
            else
                renderLink(writer, cssClass, result.getReportFilename(), linkText);
            linkText = " (" + i++ + ")";
        }
    }

    private void renderLinks(Writer writer, LayoutType layoutType, Iterable<Link> linkIterable) throws IOException
    {
        Iterator<Link> links = linkIterable.iterator();
        if (null == layoutType)
            layoutType = LayoutType.HORIZONTAL;
        switch (layoutType)
        {
            case UL:
                renderAsLI(writer, links, true);
                break;
            case LI:
                renderAsLI(writer, links, false);
                break;
            case DL:
                renderAsDT(writer, links, true);
                break;
            case DT:
                renderAsDT(writer, links, false);
                break;
            default:
                renderAsHorizontal(writer, links);
                break;
        }
    }

    private void renderLink(Writer writer, String cssClass, String href, String linkText) throws IOException
    {
        writer.append("<a");
        if (cssClass != null)
            writer.append(" class='" + cssClass + "'");
        writer.append(" href='").append(href).append("'>").append(linkText).append("</a>");
    }

    /**
     * Renders in LI tags, Wraps with UL tags optionally.
     */
    private void renderAsLI(Writer writer, Iterator<Link> links, boolean wrap) throws IOException
    {
        if (!links.hasNext())
            return;

        if (wrap)
            writer.append("<ul>");
        while (links.hasNext())
        {
            Link link = links.next();
            writer.append("<li>");
            renderLink(writer, link);
            writer.append("</li>");
        }
        if (wrap)
            writer.append("</ul>");
    }

    /*
     * Renders as DT elements, optionally wraps in DL
     */
    private void renderAsDT(Writer writer, Iterator<Link> links, boolean wrap) throws IOException
    {
        if (!links.hasNext())
            return;

        if (wrap)
            writer.append("<dl>");
        while (links.hasNext())
        {
            Link link = links.next();
            writer.append("<dt>").append(link.getDescription());
            writer.append("</dt><dd><a href='" + link.getLink() + "'>Link</a></dd>");
        }
        if (wrap)
            writer.append("</dl>");
    }

    private void renderAsHorizontal(Writer writer, Iterator<Link> links) throws IOException
    {
        if (links.hasNext())
            return;

        renderLink(writer, links.next());
        while (links.hasNext())
        {
            writer.append(" | ");
            renderLink(writer, links.next());
        }
    }

    private void renderLink(Writer writer, Link link) throws IOException
    {
        writer.append("<a href='" + link.getLink() + "'>");
        writer.append(link.getDescription());
        writer.append("</a>");
    }

    private String getPrettyPathForFile(FileModel fileModel)
    {
        if (fileModel instanceof JavaClassFileModel)
        {
            JavaClassFileModel jcfm = (JavaClassFileModel) fileModel;
            if (jcfm.getJavaClass() == null)
                return fileModel.getPrettyPathWithinProject();
            else
                return jcfm.getJavaClass().getQualifiedName();
        }
        else if (fileModel instanceof ReportResourceFileModel)
        {
            return "resources/" + fileModel.getPrettyPath();
        }
        else if (fileModel instanceof JavaSourceFileModel)
        {
            JavaSourceFileModel javaSourceModel = (JavaSourceFileModel) fileModel;
            String filename = StringUtils.removeEndIgnoreCase(fileModel.getFileName(), ".java");
            String packageName = javaSourceModel.getPackageName();
            return packageName == null || packageName.isEmpty() ? filename : packageName + "." + filename;
        }
        else
        {
            return fileModel.getPrettyPathWithinProject();
        }
    }

    @Override
    public String getDirectiveName()
    {
        return NAME;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
        this.sourceReportService = new SourceReportService(this.context);
        this.javaClassService = new JavaClassService(this.context);
    }

    private static enum LayoutType
    {
        HORIZONTAL, UL, DL, LI, DT
    }

    private static class Link
    {
        private final String link;
        private final String description;

        Link(String link, String description)
        {
            this.link = link;
            this.description = description;
        }

        private static List<Link> fromLinkModels(Iterable<LinkModel> links)
        {
            List<Link> links2 = new LinkedList<>();
            for (LinkModel model : links)
                links2.add(new Link(model.getLink(), model.getDescription()));
            return links2;
        }


        public String getLink()
        {
            return link;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
