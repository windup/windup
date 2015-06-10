package org.jboss.windup.reporting.config.condition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.condition.GraphConditionFilter;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns true if there are {@link ClassificationModel} entries that match the given classification text.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ClassificationExists  extends GraphConditionFilter<WindupVertexFrame>
{
    private String filename;
    private String classificationPattern;


    ClassificationExists(String classificationPattern)
    {
        if(classificationPattern !=null) {
            this.classificationPattern = "[\\s\\S]*" + classificationPattern + "[\\s\\S]*";
        }
    }

    /**
     * Specifies the regular expression to use when searching {@link ClassificationModel} entries.
     */
    public static ClassificationExists withClassification(String classificationPattern)
    {
        return new ClassificationExists(classificationPattern);
    }

    /**
     * Only consider entries that reference a file with the given filename.
     */
    public ClassificationExists in(String filename)
    {
        this.filename = filename;
        return this;
    }

    /**
     * Return all fileLocations placed on a file that is classified
     * @param event
     * @param context
     * @return
     */
    @Override public Iterable<? extends WindupVertexFrame> fillIn(GraphRewrite event, EvaluationContext context)
    {
        GraphService<? extends ClassificationModel> classificationService = new GraphService<>(event.getGraphContext(),ClassificationModel.class);
        Set<FileModel> input = new HashSet<>();
        for (ClassificationModel cmodel : classificationService.findAll())
        {
            for (FileModel fileModel : cmodel.getFileModels())
            {
                input.add(fileModel);

            }
        }
        return input;
    }

    @Override
    public boolean accept(GraphRewrite event, EvaluationContext context, WindupVertexFrame vertex) {
        if(vertex instanceof FileReferenceModel) {
            return accept(event,context,(FileReferenceModel)vertex);
        }
        if(vertex instanceof FileModel) {
            return accept(event,context,(FileModel)vertex);
        }
        throw new IllegalArgumentException("ClassificationExists supports only FileModel and FileReferenceModels");
    }

    private boolean accept(GraphRewrite event, EvaluationContext context, FileReferenceModel vertex) {
         return accept(event, context, vertex.getFile());
    }


    private boolean accept(GraphRewrite event, EvaluationContext context, FileModel file)
    {
        boolean result = filename==null || (file.getFileName().equals(filename));
        FramedVertexIterable<ClassificationModel> classificationsOnFile = new FramedVertexIterable<ClassificationModel>(event.getGraphContext().getFramed(),file.asVertex().getVertices(
                    Direction.IN, ClassificationModel.FILE_MODEL), ClassificationModel.class);
        result = result && classificationsOnFile.iterator().hasNext();
        if(classificationPattern !=null) {
            boolean classificationFound = false;
            for (ClassificationModel classificationModel : classificationsOnFile)
            {
                classificationFound = classificationFound || (classificationModel.getClassification().matches(classificationPattern));
            }
            result = result && classificationFound;
        }

        return result;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getClassificationPattern()
    {
        return classificationPattern;
    }

}
