package org.jboss.windup.rules.apps.javadecompiler;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class DecompileArchivesRuleProvider extends WindupRuleProvider
{
    private static Logger LOG = Logger.getLogger(DecompileArchivesRuleProvider.class.getSimpleName());

    @Inject
    private WindupConfigurationService windupConfigurationService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(AnalyzeJavaFilesRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        QueryGremlinCriterion shouldDecompileCriterion = new ShouldDecompileCriterion();
        
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.find(ArchiveModel.class).piped(shouldDecompileCriterion))
        .perform(new ProcyonDecompilerOperation());
    }
    // @formatter:on

    /**
     * A Gremlin criterion that only passes along Vertices with Java Classes that appear to be interesting (in the
     * package list that we are interested in).
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
                    for (FileModel fileModel : archive.getContainedFileModels())
                    {
                        if (fileModel instanceof JavaClassFileModel)
                        {
                            JavaClassFileModel javaClassFileModel = (JavaClassFileModel) fileModel;
                            if (windupConfigurationService.shouldScanPackage(javaClassFileModel.getPackageName()))
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