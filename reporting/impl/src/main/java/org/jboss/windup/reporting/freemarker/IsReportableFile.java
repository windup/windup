package org.jboss.windup.reporting.freemarker;

import java.util.List;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

/**
 *
 * This determines whether or not the given file is interesting to report on. This is based on whether there is an available source report as well as
 * whether or not the file has classifications or hints that pass the tag filter.
 *
 * It can be called as follows:
 *
 * <pre>
 * isReportableFile(FileModel, Setlt;String&gt; includeTags, Setlt&;String&gt; excludeTags)
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class IsReportableFile implements WindupFreeMarkerMethod
{
    public static final String NAME = "isReportableFile";
    private SourceReportService sourceReportService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.sourceReportService = new SourceReportService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + FileModel.class.getSimpleName()
                    + " and a set of tags to include and exclude as parameters, and whether or not this file should appear in overview reports";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        try
        {
            if (arguments.size() != 3)
            {
                throw new TemplateModelException("Error, method expects one argument (FileModel, includeTags:Set<String>, excludeTags:Set<String>)");
            }
            StringModel stringModelArg = (StringModel) arguments.get(0);
            FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();

            Set<String> includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(1));
            Set<String> excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(2));

            SourceReportModel result = sourceReportService.getSourceReportForFileModel(fileModel);

            if (result == null)
                return false;

            return TagUtil.hasHintsOrClassificationsWithRelevantTags(result.getSourceFileModel(), includeTags, excludeTags);
        }
        finally
        {
            ExecutionStatistics.get().end(NAME);
        }
    }

}
