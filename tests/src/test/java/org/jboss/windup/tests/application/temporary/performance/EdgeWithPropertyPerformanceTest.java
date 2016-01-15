package org.jboss.windup.tests.application.temporary.performance;

import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.jboss.windup.tests.application.temporary.performance.data.initialization.AbstractDataInitializer;
import org.jboss.windup.tests.application.temporary.performance.data.initialization.NoiseDataInitializer;
import org.jboss.windup.tests.application.temporary.performance.data.initialization.TestDataInitializer;
import org.jboss.windup.tests.application.temporary.performance.queries.TestQuery;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.PrintWriter;

/**
 * Created by mbriskar on 1/13/16.
 */
@RunWith(Arquillian.class)
public class EdgeWithPropertyPerformanceTest extends WindupArchitectureTest
{

    public static final int NUMBER_OF_FILE_MODELS_WITH_PREFIX = 50000;
    public static final int NUMBER_OF_FILE_MODELS_WITHOUT_PREFIX = 50000;

    //noise vertices
    public static final int NUMBER_OF_FILE_MODELS_NOISE = 100000;
    public static final int NUMBER_OF_CLASSIFICATIONS_NOISE = 100000;
    public static final int NUMBER_OF_HINTS_NOISE = 100000;

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addPackage(TestQuery.class.getPackage())
                    .addPackage(AbstractDataInitializer.class.getPackage())
                    .addClass(EdgeWithPropertyPerformanceTest.class);
    }

    private PrintWriter writer;
    private TestDataInitializer testDataInitializer = new TestDataInitializer(NUMBER_OF_FILE_MODELS_WITH_PREFIX,NUMBER_OF_FILE_MODELS_WITHOUT_PREFIX);
    private NoiseDataInitializer noiseDataInitializer = new NoiseDataInitializer(NUMBER_OF_FILE_MODELS_NOISE,NUMBER_OF_CLASSIFICATIONS_NOISE,NUMBER_OF_HINTS_NOISE);


    @Inject @Any
    private Imported<TestQuery> queries;


    @Test
    public void performanceEdgeTest() throws Exception
    {
        writer = new PrintWriter("performance-edge-output.txt", "UTF-8");
        writer.println();
        if(queries.isUnsatisfied()) {
            Assert.fail("No query to run found!");
        }
        for (TestQuery query : queries)
        {
            try (GraphContext context = createGraphContext())
            {
                initData(context);
                query.setContext(context);
                Iterable<FileModel> result = query.queryAndMeasureTime();
                Assert.assertEquals(NUMBER_OF_FILE_MODELS_WITH_PREFIX,Iterables.size(result));
                            writer.println(query.getTotalTimeReport());
                writer.println();
            }
        }

        writer.println();
        writer.println("INITIALIZATION TIME:");
        writer.println(testDataInitializer.getTotalReport());
        writer.close();;
    }

    private void initData(GraphContext context) {
        testDataInitializer.initData(context);
        noiseDataInitializer.initData(context);
    }




}
