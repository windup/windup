package org.jboss.windup.reporting.rules;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.BlackListModel;
import org.jboss.windup.graph.model.ClassificationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class CreateSourceReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private SourceReportService sourceReportService;

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /*
         * Find all files for which there is at least one classification or blacklist
         */
        Condition finder = Query.find(FileModel.class)
                    .piped(new QueryGremlinCriterion()
                    {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                        {
                            FramedGraph<TitanGraph> framed = event.getGraphContext().getFramed();

                            // create a pipeline to get all blacklisted items
                            GremlinPipeline<Vertex, Vertex> blacklistPipeline = new GremlinPipeline<Vertex, Vertex>(
                                        framed
                                                    .query()
                                                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource")
                                                    .vertices());
                            blacklistPipeline.as("fileModel1").in(BlackListModel.FILE_MODEL).back("fileModel1");

                            // create a pipeline to get all items with attached classifications
                            GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                                        framed
                                                    .query()
                                                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, "FileResource")
                                                    .vertices());
                            classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                                        .back("fileModel2");

                            // combine these to get all file models that have either classifications or blacklists
                            pipeline.or(blacklistPipeline, classificationPipeline);
                        }
                    })
                    .as("fileModels");

        GraphOperation addSourceReport = new AbstractIterationOperation<FileModel>(FileModel.class, "fileModel")
        {
            public void perform(GraphRewrite event, EvaluationContext context,
                        FileModel payload)
            {
                SourceReportModel sm = sourceReportService.create();
                sm.setSourceFileModel(payload);
                sm.setReportName(payload.getPrettyPath());
                sm.setSourceType(resolveSourceType(payload));
            }
        };

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(finder)
                    .perform(
                                Iteration.over("fileModels")
                                            .as("fileModel")
                                            .perform(addSourceReport).endIteration());
    }

    private String resolveSourceType(FileModel f)
    {
        for (SourceTypeResolver resolver : resolvers)
        {
            String resolvedType = resolver.resolveSourceType(f);
            if (resolvedType != null)
            {
                return resolvedType;
            }
        }
        return "unknown";
    }
}
