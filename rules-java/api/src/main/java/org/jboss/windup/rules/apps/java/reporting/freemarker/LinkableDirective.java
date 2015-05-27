package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerTemplateDirective;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.util.Logging;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * Renders linkable elements as a list of links
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class LinkableDirective implements WindupFreeMarkerTemplateDirective
{
    private static final Logger LOG = Logging.get(LinkableDirective.class);
    private GraphContext context;

    @Override
    public String getDescription()
    {
        return "Takes the following parameters: LinkableModel (a " + LinkableModel.class.getSimpleName() +")";
    }

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
                TemplateDirectiveBody body)
                throws TemplateException, IOException
    {
        
        final Writer writer = env.getOut();

        StringModel projectStringModel = (StringModel) params.get("linkable");
        Object obj = projectStringModel.getWrappedObject();

        if (!(obj instanceof LinkableModel)) 
        {
            return;
        }

        LayoutType layoutType = LayoutType.HORIZONTAL;
        SimpleScalar layoutModel = (SimpleScalar) params.get("layout");
        if (layoutModel != null)
        {
            String lt = layoutModel.getAsString();
            try
            {
                LayoutType.valueOf(lt.toUpperCase());

            }
            catch (IllegalArgumentException e)
            {
                throw new TemplateException("Layout: " + lt + " is not supported.", e, null);
            }
        }

        LinkableModel linkable = (LinkableModel) obj;
        
        if(layoutType == LayoutType.UL) {
            renderAsUL(writer, linkable);
        }
        if(layoutType == LayoutType.LI) {
            renderAsLI(writer, linkable);
        }
        else if(layoutType == LayoutType.DL) {
            renderAsDL(writer, linkable);
        }
        else if(layoutType == LayoutType.DT) {
            renderAsDT(writer, linkable);
        }
        else {
            renderAsHorizontal(writer, linkable);
        }

    }

    /*
     * Wraps with UL tags
     */
    private void renderAsUL(Writer writer, LinkableModel linkable) throws IOException
    {
        Iterator<LinkModel> links = linkable.getLinks().iterator();
        if(links.hasNext()) {
            writer.append("<ul>");
            renderAsLI(writer, linkable);
            writer.append("</ul>");
        }
    }
    
    /*
     * Renders only LI tags
     */
    private void renderAsLI(Writer writer, LinkableModel linkable) throws IOException
    {
        Iterator<LinkModel> links = linkable.getLinks().iterator();
        if(links.hasNext()) {
            while (links.hasNext())
            {
                LinkModel link = links.next();
                writer.append("<li>");
                renderLink(writer, link);
                writer.append("</li>");
            }
        }
    }

    /*
     * Renders the full DL
     */
    private void renderAsDL(Writer writer, LinkableModel linkable) throws IOException
    {
        Iterator<LinkModel> links = linkable.getLinks().iterator();
        if(links.hasNext()) {
            writer.append("<dl>");
            renderAsDT(writer, linkable);
            writer.append("</dl>");
        }
    }
    
    /*
     * Renders as DT elements
     */
    private void renderAsDT(Writer writer, LinkableModel linkable) throws IOException
    {
        Iterator<LinkModel> links = linkable.getLinks().iterator();
        if(links.hasNext()) {
            while (links.hasNext())
            {
                LinkModel link = links.next();
                writer.append("<dt>");
                    writer.append(link.getDescription());
                writer.append("</dt>");
                writer.append("<dd>");
                    writer.append("<a href='" + link.getLink() + "'>Link</a>");
                writer.append("</dd>");
            }
        }
    }

    
    private void renderAsHorizontal(Writer writer, LinkableModel linkable) throws IOException
    {
        Iterator<LinkModel> links = linkable.getLinks().iterator();
        while (links.hasNext())
        {
            LinkModel link = links.next();
            renderLink(writer, link);

            if (links.hasNext())
            {
                writer.append(" | ");
            }
        }
    }

    private void renderLink(Writer writer, LinkModel link) throws IOException
    {
        writer.append("<a href='" + link.getLink() + "'>");
        writer.append(link.getDescription());
        writer.append("</a>");
    }

    @Override
    public String getDirectiveName()
    {
        return "render_linkable";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    private static enum LayoutType
    {
        HORIZONTAL, UL, DL, LI, DT
    }
}
