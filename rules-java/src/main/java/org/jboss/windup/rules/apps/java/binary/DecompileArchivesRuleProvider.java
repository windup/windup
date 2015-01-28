package org.jboss.windup.rules.apps.java.binary;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.phase.Decompilation;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class DecompileArchivesRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Decompilation.class;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        QueryGremlinCriterion shouldDecompileCriterion = new ShouldDecompileCriterion();
        
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(ArchiveModel.class).piped(shouldDecompileCriterion))
        .perform(
            new ProcyonDecompilerOperation()
            .and(IterationProgress.monitoring("Decompiled archive: ", 1))
            .and(Commit.every(1))
        )
        .otherwise(Log.message(Level.WARN, "No archives to decompile."));
    }
    // @formatter:on

    /**
     * A Gremlin criterion that only passes along Vertices with Java Classes that appear to be interesting (in the package list that we are interested
     * in).
     */
    private class ShouldDecompileCriterion implements QueryGremlinCriterion
    {
        @Override
        public void query(final GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
        {
            pipeline.filter(new PipeFunction<Vertex, Boolean>()
            {
                @Override
                public Boolean compute(Vertex argument)
                {
                    ArchiveModel archive = event.getGraphContext().getFramed().frame(argument, ArchiveModel.class);
                    WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(
                                event.getGraphContext());
                    for (FileModel fileModel : archive.getContainedFileModels())
                    {
                        if (fileModel instanceof JavaClassFileModel)
                        {
                            JavaClassFileModel javaClassFileModel = (JavaClassFileModel) fileModel;
                            if (windupJavaConfigurationService.shouldScanPackage(javaClassFileModel.getPackageName()))
                            {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }
}