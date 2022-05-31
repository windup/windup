package org.jboss.windup.reporting.freemarker;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gets all tags from the classifications associated with the provided {@link FileModel}.
 * <p>
 * Example call:
 * <p>
 * getTagsFromFileClassificationsAndHints(FileModel).
 * <p>
 * The method will return a {@link Set}<String> instance.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GetTagsFromFileClassificationsAndHints implements WindupFreeMarkerMethod {
    private static final String NAME = "getTagsFromFileClassificationsAndHints";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
            throw new TemplateModelException("Error, method expects one argument (" + FileModel.class.getSimpleName() + ")");

        StringModel stringModelArg = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();
        Set<String> tags = this.findTagsFromFileClassificationsAndHints(fileModel);
        ExecutionStatistics.get().end(NAME);
        return tags;
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + FileModel.class.getSimpleName()
                + " as a parameter and returns an Set<String> containing the tags"
                + " from the classifications associated with the provided this file.";
    }


    private Set<String> findTagsFromFileClassificationsAndHints(FileModel fileModel) {
        Set<String> tags = new HashSet<>();

        // Classifications
        {
            GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(context.getGraph()).V(fileModel.getElement());
            pipeline.in(ClassificationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.textContains(ClassificationModel.TYPE));
            FramedVertexIterable<ClassificationModel> iterable = new FramedVertexIterable<>(this.context.getFramed(), pipeline.toList(), ClassificationModel.class);
            for (ClassificationModel classification : iterable)
                tags.addAll(classification.getTags());
        }

        // Hints
        {
            GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(context.getGraph()).V(fileModel.getElement());
            pipeline.in(FileLocationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.textContains(FileLocationModel.TYPE));
            pipeline.in(InlineHintModel.FILE_LOCATION_REFERENCE).has(WindupVertexFrame.TYPE_PROP, Text.textContains(InlineHintModel.TYPE));
            FramedVertexIterable<InlineHintModel> iterable = new FramedVertexIterable<>(this.context.getFramed(), pipeline.toList(), InlineHintModel.class);
            for (InlineHintModel hint : iterable)
                tags.addAll(hint.getTags());
        }

        return tags;
    }
}
