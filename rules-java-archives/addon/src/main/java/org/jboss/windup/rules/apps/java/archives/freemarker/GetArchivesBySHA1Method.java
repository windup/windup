package org.jboss.windup.rules.apps.java.archives.freemarker;

import java.util.List;

import freemarker.template.SimpleScalar;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import freemarker.template.TemplateModelException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetArchivesBySHA1Method implements WindupFreeMarkerMethod
{
    private static final String NAME = "getArchivesBySHA1";
    private static final String DESCRIPTION = "Takes a single String parameter (SHA1 Hash) and returns an Iterable of ArchiveModel instances with the given Hash";

    private GraphContext context;

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (String)");
        }
        SimpleScalar freemarkerArg = (SimpleScalar) arguments.get(0);
        String sha1 = freemarkerArg.getAsString();
        ArchiveService archiveService = new ArchiveService(context);
        return archiveService.findBySHA1(sha1);
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }
}
