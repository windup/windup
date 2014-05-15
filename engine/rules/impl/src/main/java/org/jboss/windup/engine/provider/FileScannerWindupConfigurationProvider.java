package org.jboss.windup.engine.provider;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchGremlinCriterion;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.AddArchiveReferenceInformation;
import org.jboss.windup.addon.config.operation.ruleelement.TypeOperation;
import org.jboss.windup.engine.util.ZipUtil;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class FileScannerWindupConfigurationProvider extends WindupConfigurationProvider
{
    @Inject
    ApplicationReferenceDao applicationReferenceDao;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                                GraphSearchConditionBuilder
                                            .create("inputFiles")
                                            .ofType(FileResourceModel.class)
                                            .gremlin()
                                            .withCriterion(new GraphSearchGremlinCriterion()
                                            {

                                                @Override
                                                public void query(GremlinPipeline<Vertex, Vertex> pipeline)
                                                {
                                                    pipeline.has("filePath", Text.REGEX,
                                                                ZipUtil.getEndsWithZipRegularExpression());
                                                }
                                            })

                    )
                    .perform(
                                Iteration.over("inputFiles").var(FileResourceModel.class, "file")
                                            .perform(TypeOperation.addType("file", ArchiveResourceModel.class)
                                                        .and(AddArchiveReferenceInformation.addReferenceInformation()
                                                        )
                                            )
                    );
    }
}
