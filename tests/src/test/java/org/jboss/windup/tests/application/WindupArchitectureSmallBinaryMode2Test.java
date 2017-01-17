package org.jboss.windup.tests.application;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.stats.ProjectTechnologiesStatsModel;
import org.jboss.windup.rules.apps.javaee.model.stats.TechnologiesStatsModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSmallBinaryMode2Test extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupTiny() throws Exception
    {
        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, "../test-files/Windup1x-javaee-example-tiny.war", false);
            validateTechReportData(context);
            validateArchiveHashes(context);
            validateJavaClassModels(context);
        }
    }

    private void validateTechReportData(GraphContext context)
    {
        Iterable<ProjectTechnologiesStatsModel> stats = context.service(ProjectTechnologiesStatsModel.class).findAll();

        //SUM: 106 txt = 1 java = 20 xml = 12 ear = 0 war = 0 MF = 5 jar = 5 class = 47 properties = 4
        HashMap<String, Integer> sumResultsFileTypes = new HashMap<>();
        HashMap<String, Integer> sumResultsTechnologies = new HashMap<>();

        for (ProjectTechnologiesStatsModel projectStats : stats)
        {
            addDataToSumMap(projectStats.getTechnologiesStatsModel().getTechnologiesMap(), sumResultsTechnologies);
            addDataToSumMap(projectStats.getTechnologiesStatsModel().getFileTypesMap(), sumResultsFileTypes);

            Assert.assertTrue(projectStats.getComputed() != null);
        }
        
        Assert.assertEquals(1, sumResultsFileTypes.getOrDefault("class", 0) + 0);
        Assert.assertTrue(sumResultsTechnologies.getOrDefault(TechnologiesStatsModel.STATS_JAVA_CLASSES_TOTAL, 0) > 0);
    }

    private void addDataToSumMap(Map<String, Integer> sourceMap, HashMap<String, Integer> resultMap)
    {
        sourceMap.entrySet().forEach(item -> {
            String key = item.getKey();
            resultMap.put(key, resultMap.getOrDefault(key, 0) + item.getValue());
        });
    }

    private void validateArchiveHashes(GraphContext context) throws Exception
    {
        ArchiveService archiveService = new ArchiveService(context);
        int numberFound = 0;
        for (ArchiveModel model : archiveService.findAll())
        {
            numberFound++;

            Assert.assertEquals("c60bb0c51623a915cb4a9a90ba9ba70e", model.getMD5Hash());
            Assert.assertEquals("1a1888023eff8629a9e55f023c8ecf63f69fad03", model.getSHA1Hash());
        }
        Assert.assertEquals(1, numberFound);
    }

    private void validateJavaClassModels(GraphContext context)
    {
        JavaClassService service = new JavaClassService(context);

        boolean servletClassFound = false;
        for (JavaClassModel model : service.findAll())
        {
            if (model.getQualifiedName().equals("org.windup.examples.servlet.SampleServlet"))
            {
                servletClassFound = true;
            }
        }
        Assert.assertTrue(servletClassFound);
    }
}
