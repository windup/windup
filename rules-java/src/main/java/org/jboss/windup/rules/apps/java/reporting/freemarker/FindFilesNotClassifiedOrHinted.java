package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.query.FindFilesNotClassifiedOrHintedGremlinCriterion;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.ExecutionStatistics;

import com.tinkerpop.blueprints.Vertex;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Finds the files that have not had {@link ClassificationModel}s linked, and also does not have {@link FileLocationModel}s linked.
 * 
 * Called by:
 * 
 * findFilesNotClassifiedOrHinted(Iterable<FileModel>)
 * 
 * NOTE: This will only return JavaSourceFileModels and XmlFileModels in order to reduce clutter.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class FindFilesNotClassifiedOrHinted implements WindupFreeMarkerMethod
{
    private static final String NAME = "findFilesNotClassifiedOrHinted";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Iterable<FileModel>)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        @SuppressWarnings("unchecked")
        Iterable<FileModel> fileModels = (Iterable<FileModel>) stringModelArg.getWrappedObject();

        FindFilesNotClassifiedOrHintedGremlinCriterion criterion = new FindFilesNotClassifiedOrHintedGremlinCriterion();
        List<Vertex> initialFileModelsAsVertices = new ArrayList<>();
        for (FileModel fm : fileModels)
        {
            initialFileModelsAsVertices.add(fm.asVertex());
        }
        Iterable<Vertex> result = criterion.query(context, initialFileModelsAsVertices);

        List<FileModel> resultModels = new ArrayList<FileModel>();
        for (Vertex v : result)
        {
            FileModel f = context.getFramed().frame(v, FileModel.class);
            if (f instanceof JavaSourceFileModel || f instanceof XmlFileModel)
            {
                resultModels.add(f);
            }
        }

        ExecutionStatistics.get().end(NAME);
        return resultModels;
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes an Iterable<" + FileModel.class.getSimpleName()
                    + "> as a parameter and returns the files that have neither " + ClassificationModel.class.getSimpleName()
                    + "s nor " + InlineHintModel.class.getSimpleName() + "s associated with them.";
    }
}
