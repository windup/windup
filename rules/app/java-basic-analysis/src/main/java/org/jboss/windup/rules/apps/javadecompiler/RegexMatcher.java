package org.jboss.windup.rules.apps.javadecompiler;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AddClassFileMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.rules.apps.javascanner.provider.DiscoverJavaFilesConfigurationProvider;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class RegexMatcher extends WindupConfigurationProvider
{
    @Override public RulePhase getPhase(){
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return generateDependencies(DiscoverJavaFilesConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(
                    GraphSearchConditionBuilderGremlin.create("javaFiles", new ArrayList())
                    .V().framedType( JavaClassModel.class )
            )
            .perform(
                Iteration.over("javaFiles").var("javaFile")
                    .perform( new AddClassFileMetadata("classFile") )
                .endIteration()
            );
    }
}
