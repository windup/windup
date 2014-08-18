package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.query.FindFilesNotClassifiedOrHintedGremlinCriterion;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Vertex;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Finds the files that have not had {@link ClassificationModel}s linked, and also does not have
 * {@link FileLocationModel}s linked.
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
    @Inject
    private GraphContext context;

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
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

        return resultModels;
    }

    @Override
    public String getMethodName()
    {
        return "findFilesNotClassifiedOrHinted";
    }

}
