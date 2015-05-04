package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.ResourceModel;
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
 * findFilesNotClassifiedOrHinted(Iterable<ResourceModel>)
 * 
 * NOTE: This will only return JavaSourceFileModels and XmlFileModels in order to reduce clutter.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
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
            throw new TemplateModelException("Error, method expects one argument (Iterable<ResourceModel>)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        @SuppressWarnings("unchecked")
        Iterable<ResourceModel> fileModels = (Iterable<ResourceModel>) stringModelArg.getWrappedObject();

        FindFilesNotClassifiedOrHintedGremlinCriterion criterion = new FindFilesNotClassifiedOrHintedGremlinCriterion();
        List<Vertex> initialResourceModelsAsVertices = new ArrayList<>();
        for (ResourceModel fm : fileModels)
        {
            initialResourceModelsAsVertices.add(fm.asVertex());
        }
        Iterable<Vertex> result = criterion.query(context, initialResourceModelsAsVertices);

        List<ResourceModel> resultModels = new ArrayList<ResourceModel>();
        for (Vertex v : result)
        {
            ResourceModel f = context.getFramed().frame(v, ResourceModel.class);
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
        return "Takes an Iterable<" + ResourceModel.class.getSimpleName()
                    + "> as a parameter and returns the files that have neither " + ClassificationModel.class.getSimpleName()
                    + "s nor " + InlineHintModel.class.getSimpleName() + "s associated with them.";
    }
}
