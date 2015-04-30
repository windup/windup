package org.jboss.windup.tests.application;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.ApplicationFlag;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClasses(WindupArchitectureTest.class,WindupArchitectureMediumBinaryModeTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-tattletale"),
                                AddonDependencyEntry.create("org.jboss.windup.tests:test-util"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-groovy")
                    );
    }

    @Inject
    JavaClassTestRuleProvider rp;
    File resultFile = new File("/home/mbriskar/performance.txt");
    @Test
    public void testRunWindupMedium() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, false);
            System.out.println(rp.firstRuleMatchCount);
            PrintWriter writer = new PrintWriter(new FileWriter(resultFile,true));
            writer.println();
            writer.append("The first rule using String flag results:");
            writer.println();
            writer.append("String flag match count: " + rp.firstRuleMatchCount);
            writer.println();
            writer.append("String flag time: " + rp.firstExecution);
            writer.println();
            writer.append("Edge flag match count: " + rp.secondRuleMatchCount);
            writer.println();
            writer.append("Edge flag time: " + rp.secondExecution);
            writer.println();
            writer.close();
            System.out.println(rp.secondRuleMatchCount);
            System.out.println("finish");
        }
    }
    
    @Test
    public void testRunWindupMedium2() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, false);
            System.out.println(rp.firstRuleMatchCount);
            PrintWriter writer = new PrintWriter(new FileWriter(resultFile,true));
            writer.println();
            writer.append("The first rule using String flag results:");
            writer.println();
            writer.append("String flag match count: " + rp.firstRuleMatchCount);
            writer.println();
            writer.append("String flag time: " + rp.firstExecution);
            writer.println();
            writer.append("Edge flag match count: " + rp.secondRuleMatchCount);
            writer.println();
            writer.append("Edge flag time: " + rp.secondExecution);
            writer.println();
            writer.close();
            System.out.println(rp.secondRuleMatchCount);
            System.out.println("finish");
        }
    }
    
    @Test
    public void testRunWindupMedium3() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, false);
            System.out.println(rp.firstRuleMatchCount);
            PrintWriter writer = new PrintWriter(new FileWriter(resultFile,true));
            writer.println();
            writer.append("The first rule using String flag results:");
            writer.println();
            writer.append("String flag match count: " + rp.firstRuleMatchCount);
            writer.println();
            writer.append("String flag time: " + rp.firstExecution);
            writer.println();
            writer.append("Edge flag match count: " + rp.secondRuleMatchCount);
            writer.println();
            writer.append("Edge flag time: " + rp.secondExecution);
            writer.println();
            writer.close();
            System.out.println(rp.secondRuleMatchCount);
            System.out.println("finish");
        }
    }

    @Singleton
    public static class JavaClassTestRuleProvider extends AbstractRuleProvider
    {
        private static Logger log = Logger.getLogger(RuleSubset.class.getName());

        private int firstRuleMatchCount = 0;
        private long firstExecution = 0;

        private int secondRuleMatchCount = 0;
        private long secondExecution = 0;

        public JavaClassTestRuleProvider()
        {
            super(MetadataBuilder.forProvider(JavaClassTestRuleProvider.class)
                        .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
                        .addRule().perform(new GraphOperation()
                            {

                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context)
                                {
                                    firstExecution=System.currentTimeMillis();
                                }
                                
                            }
                        )
            .addRule().when(
                        Query.fromType(FileModel.class).withProperty(FileModel.APPLICATION_FLAG, "application")
            ).perform(
                Iteration.over().perform(new AbstractIterationOperation<FileModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
                    {
                        firstRuleMatchCount++;
                    }
                }).endIteration()
            )
            .addRule().perform(
                            new GraphOperation()
                            {

                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context)
                                {
                                    firstExecution=System.currentTimeMillis() - firstExecution;
                                }
                                
                            }
                        )
                        
                        
                        
                        
                        
                        
                        
                        //now the edge
                        
                        
                        
                        
                        .addRule().perform(
                            new GraphOperation()
                            {

                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context)
                                {
                                    secondExecution=System.currentTimeMillis();
                                }
                                
                            }
                        )
            .addRule().when(
                        Query.fromType(FileModel.class).piped(new QueryGremlinCriterion() {

                            @Override
                            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                            {
                                pipeline.as("result").out("ApplicationFlagVertex").back("result");
                            }
                            
                        })
            ).perform(
                Iteration.over().perform(new AbstractIterationOperation<FileModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
                    {
                        secondRuleMatchCount++;
                    }
                }).endIteration()
            )
            .addRule().perform(
                           new GraphOperation()
                            {

                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context)
                                {
                                    secondExecution=System.currentTimeMillis() - secondExecution;
                                }
                                
                            })
                        ;
        }
        // @formatter:on

        public int getFirstRuleMatchCount()
        {
            return firstRuleMatchCount;
        }

        public int getSecondRuleMatchCount()
        {
            return secondRuleMatchCount;
        }

    }

}
