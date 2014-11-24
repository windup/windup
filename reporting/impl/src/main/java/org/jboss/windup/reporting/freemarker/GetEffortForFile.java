package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Gets the number of effort points involved in migrating this particular file
 * 
 * Called from a freemarker template as follows:
 * 
 * getMigrationEffortPointsForFile(FileModel):int
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class GetEffortForFile implements WindupFreeMarkerMethod
{
    private static final String NAME = "getMigrationEffortPointsForFile";
    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.classificationService = new ClassificationService(event.getGraphContext());
        this.inlineHintService = new InlineHintService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + FileModel.class.getSimpleName() + " as a parameter and returns an int containing the effort estimate for this file.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException(
                        "Error, method expects one argument (FileModel)");
        }
        StringModel fileModelArg = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) fileModelArg.getWrappedObject();

        Object result = classificationService.getMigrationEffortPoints(fileModel) + inlineHintService.getMigrationEffortPoints(fileModel);
        ExecutionStatistics.get().end(NAME);
        return result;
    }
}
