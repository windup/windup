package org.jboss.windup.rules.apps.java.reporting.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.util.IterableConverter;
import org.jboss.windup.util.Logging;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Renders linkable elements as a list of links.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class RenderLinkDirective implements WindupFreeMarkerTemplateDirective {
    public static final String NAME = "render_link";
    public static final String MODEL = "model";
    public static final String TEXT = "text";
    public static final String PROJECT = "project";
    private static final Logger LOG = Logging.get(RenderLinkDirective.class);
    private GraphContext context;
    private SourceReportService sourceReportService;
    private JavaClassService javaClassService;

    @Override
    public String getDescription() {
        return "Takes the following parameters: FileModel (a " + FileModel.class.getSimpleName() + ")";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        final Writer writer = env.getOut();
        StringModel stringModel = (StringModel) params.get(MODEL);

        SimpleScalar defaultTextModel = (SimpleScalar) params.get(TEXT);
        String defaultText = defaultTextModel == null ? null : defaultTextModel.getAsString();

        if (stringModel == null || stringModel.getWrappedObject() == null) {
            if (StringUtils.isNotBlank(defaultText))
                writer.append(defaultText);
            else {
                writer.append("unknown");
                LOG.warning("Failed to resolve name or text for " + getClass().getName() + ". " + env);
            }
            return;
        }

        StringModel projectStringModel = (StringModel) params.get(PROJECT);
        ProjectModel project = null;
        if (projectStringModel != null && projectStringModel.getWrappedObject() instanceof ProjectModel)
            project = (ProjectModel) projectStringModel.getWrappedObject();

        Object model = stringModel.getWrappedObject();

        LayoutType layoutType = resolveLayoutType(params);
        String cssClass = resolveCssClass(params);

        if (model instanceof FileLocationModel) {
            processFileLocationModel(writer, cssClass, project, (FileLocationModel) model, defaultText);
        } else if (model instanceof FileModel) {
            processFileModel(writer, cssClass, project, (FileModel) model, defaultText);
        } else if (model instanceof JavaClassModel) {
            processJavaClassModel(writer, cssClass, project, (JavaClassModel) model, defaultText);
        } else if (model instanceof LinkableModel) {
            processLinkableModel(writer, layoutType, cssClass, project, (LinkableModel) model, defaultText);
        } else {
            throw new TemplateException("Object type not permitted: " + model.getClass().getSimpleName(), env);
        }
    }

    private String resolveCssClass(Map params) {
        SimpleScalar css = (SimpleScalar) params.get("class");
        if (css == null)
            return "";
        else
            return css.getAsString();
    }

    private LayoutType resolveLayoutType(Map params) throws TemplateException {
        LayoutType layoutType = LayoutType.HORIZONTAL;
        SimpleScalar layoutModel = (SimpleScalar) params.get("layout");
        if (layoutModel != null) {
            String lt = layoutModel.getAsString();
            try {
                layoutType = LayoutType.valueOf(lt.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new TemplateException("Layout: " + lt + " is not supported.", e, null);
            }
        }
        return layoutType;
    }

    private void processFileLocationModel(Writer writer, String cssClass, ProjectModel project, FileLocationModel obj, String defaultText) throws IOException {
        String position = " (" + obj.getLineNumber() + ", " + obj.getColumnNumber() + ")";
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(obj.getFile()) + position : defaultText;
        String anchor = obj.getId().toString();

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(obj.getFile());
        if (result == null)
            writer.write(linkText);
        else
            renderLink(writer, cssClass, project, result.getReportFilename(), anchor, linkText);
    }

    private void processLinkableModel(Writer writer, LayoutType layoutType, String cssClass, ProjectModel project, LinkableModel obj, String defaultText) throws IOException {
        Iterable<Link> links = new IterableConverter<LinkModel, Link>(obj.getLinks()) {
            public Link from(LinkModel m) {
                return new Link(m.getLink(), m.getDescription());
            }
        };
        renderLinks(writer, layoutType, project, links);
    }

    private void processFileModel(Writer writer, String cssClass, ProjectModel project, FileModel fileModel, String defaultText) throws IOException {
        String linkText = StringUtils.isBlank(defaultText) ? getPrettyPathForFile(fileModel) : defaultText;

        SourceReportModel result = sourceReportService.getSourceReportForFileModel(fileModel);
        if (result == null)
            writer.write(linkText);
        else
            renderLink(writer, cssClass, project, result.getReportFilename(), null, linkText);
    }

    private void processJavaClassModel(Writer writer, String cssClass, ProjectModel project, JavaClassModel clz, String defaultText)
            throws IOException {
        Iterator<AbstractJavaSourceModel> results = javaClassService.getJavaSource(clz.getQualifiedName()).iterator();

        if (!results.hasNext()) {
            writer.write(clz.getQualifiedName());
            return;
        }

        String linkText = StringUtils.isBlank(defaultText) ? clz.getQualifiedName() : defaultText;
        int i = 2;
        while (results.hasNext()) {
            AbstractJavaSourceModel source = results.next();
            SourceReportModel result = sourceReportService.getSourceReportForFileModel(source);
            if (result == null)
                writer.write(linkText);
            else
                renderLink(writer, cssClass, project, result.getReportFilename(), null, linkText);
            linkText = " (" + i++ + ")";
        }
    }

    private void renderLinks(Writer writer, LayoutType layoutType, ProjectModel project, Iterable<Link> linkIterable) throws IOException {
        Iterator<Link> links = linkIterable.iterator();
        if (null == layoutType)
            layoutType = LayoutType.HORIZONTAL;
        switch (layoutType) {
            case UL:
                renderAsLI(writer, project, links, true);
                break;
            case LI:
                renderAsLI(writer, project, links, false);
                break;
            case DL:
                renderAsDT(writer, project, links, true);
                break;
            case DT:
                renderAsDT(writer, project, links, false);
                break;
            default:
                renderAsHorizontal(writer, project, links);
                break;
        }
    }

    private void renderLink(Writer writer, String cssClass, ProjectModel project, String href, String anchorId, String linkText) throws IOException {
        writer.append("<a");
        if (cssClass != null)
            writer.append(" class='" + cssClass + "'");
        writer.append(" href='").append(href);
        appendProject(writer, project);
        if (anchorId != null) {
            writer.append("#" + anchorId);
        }
        writer.append("'>").append(linkText).append("</a>");
    }

    /**
     * Renders in LI tags, Wraps with UL tags optionally.
     */
    private void renderAsLI(Writer writer, ProjectModel project, Iterator<Link> links, boolean wrap) throws IOException {
        if (!links.hasNext())
            return;

        if (wrap)
            writer.append("<ul>");
        while (links.hasNext()) {
            Link link = links.next();
            writer.append("<li>");
            renderLink(writer, project, link);
            writer.append("</li>");
        }
        if (wrap)
            writer.append("</ul>");
    }

    /*
     * Renders as DT elements, optionally wraps in DL
     */
    private void renderAsDT(Writer writer, ProjectModel project, Iterator<Link> links, boolean wrap) throws IOException {
        if (!links.hasNext())
            return;

        if (wrap)
            writer.append("<dl>");
        while (links.hasNext()) {
            Link link = links.next();
            writer.append("<dt>").append(link.getDescription());
            writer.append("</dt><dd><a href='").append(link.getLink());
            appendProject(writer, project);
            writer.append("'>Link</a></dd>");
        }
        if (wrap)
            writer.append("</dl>");
    }

    private void appendProject(Writer writer, ProjectModel project) throws IOException {
        if (project != null)
            writer.append("?project=").append(String.valueOf((Object) project.getId()));
    }

    private void renderAsHorizontal(Writer writer, ProjectModel project, Iterator<Link> links) throws IOException {
        if (links.hasNext())
            return;

        renderLink(writer, project, links.next());
        while (links.hasNext()) {
            writer.append(" | ");
            renderLink(writer, project, links.next());
        }
    }

    private void renderLink(Writer writer, ProjectModel project, Link link) throws IOException {
        writer.append("<a href='").append(link.getLink());
        appendProject(writer, project);
        writer.append("' target='_blank'>");
        writer.append(link.getDescription());
        writer.append("</a>");
    }

    private String getPrettyPathForFile(FileModel fileModel) {
        if (fileModel instanceof JavaClassFileModel) {
            JavaClassFileModel jcfm = (JavaClassFileModel) fileModel;
            if (jcfm.getJavaClass() == null)
                return fileModel.getPrettyPathWithinProject();
            else
                return jcfm.getJavaClass().getQualifiedName();
        } else if (fileModel instanceof ReportResourceFileModel) {
            return "resources/" + fileModel.getPrettyPath();
        } else if (fileModel instanceof JavaSourceFileModel) {
            JavaSourceFileModel javaSourceModel = (JavaSourceFileModel) fileModel;
            String filename = StringUtils.removeEndIgnoreCase(fileModel.getFileName(), ".java");
            String packageName = javaSourceModel.getPackageName();
            return packageName == null || packageName.isEmpty() ? filename : packageName + "." + filename;
        }
        // This is used for instance when showing unparsable files in the Issues Report.
        else if (fileModel instanceof ArchiveModel) {
            return fileModel.getPrettyPath();
        } else {
            return fileModel.getPrettyPathWithinProject();
        }
    }

    @Override
    public String getDirectiveName() {
        return NAME;
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
        this.sourceReportService = new SourceReportService(this.context);
        this.javaClassService = new JavaClassService(this.context);
    }

    private static enum LayoutType {
        HORIZONTAL, UL, DL, LI, DT
    }

    private static class Link {
        private final String link;
        private final String description;

        Link(String link, String description) {
            this.link = link;
            this.description = description;
        }

        private static List<Link> fromLinkModels(Iterable<LinkModel> links) {
            List<Link> links2 = new LinkedList<>();
            for (LinkModel model : links)
                links2.add(new Link(model.getLink(), model.getDescription()));
            return links2;
        }


        public String getLink() {
            return link;
        }

        public String getDescription() {
            return description;
        }
    }
}
