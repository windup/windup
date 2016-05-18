package org.jboss.windup.rules.apps.java.archives.freemarker;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import freemarker.template.SimpleScalar;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import freemarker.template.TemplateModelException;

/**
 * This class gets all archives by their SHA1 hash.
 *
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

        // filter out the one in the shared-libs virtual app.
        // This needs to be updated as this isn't really the right solution here to get the Jar depedency report
        // to fully work.
        Predicate<ArchiveModel> predicate = new Predicate<ArchiveModel>()
        {
            @Override
            public boolean apply(ArchiveModel input)
            {
                return !input.getDuplicateArchives().iterator().hasNext();
            }
        };
        return Iterables.filter(archiveService.findBySHA1(sha1), predicate);
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
