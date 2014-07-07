package org.jboss.windup.rules.apps.java.basic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.GraphSubset;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.scan.model.JavaFileModel;
import org.jboss.windup.rules.apps.java.scan.provider.DiscoverJavaFilesRuleProvider;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;

public class RegexMatcherRuleProvider extends WindupRuleProvider
{
    @Override public RulePhase getPhase(){
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(DiscoverJavaFilesRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(
                GraphSearchConditionBuilderGremlin.create("javaFiles", new ArrayList<Vertex>())
                .V().framedType( JavaFileModel.class ).has("analyze")
            )
            .perform(
                Iteration.over("javaFiles").as("javaFile")
                    .perform(
                        // A nested rule.
                        GraphSubset.evaluate(
                            ConfigurationBuilder.begin().addRule()
                                .when(
                                    GraphSearchConditionBuilder.create("regexes")
                                        .ofType(RegexModel.class)
                                        .withProperty( RegexModel.FOR_LANG, "java")
                                )
                                .perform(
                                    Iteration.over("regexes").as(RegexModel.class, "regex")
                                    .perform(
                                        new AbstractIterationOperation<RegexModel>( RegexModel.class, "regex") {
                                            @Override
                                            public void perform( GraphRewrite event, EvaluationContext context, RegexModel regex ) {
                                                
                                                VarStack sf = VarStack.instance(event);
                                                JavaFileModel javaFile = Iteration.getCurrentPayload(sf, JavaFileModel.class, "javaFile");
                                                
                                                getRegexMatches( javaFile.getFilePath(), regex.getRegex() );
                                            }
                                        }
                                    )
                                    .endIteration()
                                )// perform()
                                )
                                    )
                                    .endIteration()
            );
    }



    /**
     *  Returns all regex matches in the given file.
     *  TODO: Perhaps keep the compiled regex'es.
     */
    private void getRegexMatches( String filePath, String regex ) {
        File file = new File(filePath);
        if( ! file.exists() )
            throw new WindupException("File doesn't exist: " + filePath);
            
        String src;
        try {
            src = FileUtils.readFileToString( file, getEncoding());
        } catch( IOException ex ) {
            throw new WindupException("Error reading: " + file + " " + ex.getMessage(), ex);
        }

        try {
            Pattern pat = Pattern.compile( regex );
            Matcher mat = pat.matcher( src );
        } catch( Exception ex ) {
            throw new WindupException("Error when matching regex: " + ex.getMessage(), ex);
        }
    }
    
    private static String getEncoding() {
        return "UTF-8";
    }

}
