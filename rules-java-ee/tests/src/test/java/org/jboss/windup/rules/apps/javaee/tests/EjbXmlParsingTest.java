package org.jboss.windup.rules.apps.javaee.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.service.EnvironmentReferenceService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test XML parsing of different vendors.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RunWith(Arquillian.class)
public class EjbXmlParsingTest extends AbstractTest {
    private static final String WEBLOGIC_TEST_EJB_XMLS = "../../test-files/ejb/weblogic-ejb-test";
    private static final String WEBSPHERE_TEST_EJB_XMLS = "../../test-files/ejb/websphere-ejb-test";
    private static final String JBOSS_TEST_EJB_XMLS = "../../test-files/ejb/jboss-ejb-test";
    private static final String ORION_TEST_EJB_XMLS = "../../test-files/ejb/orion-ejb-test";

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testEJBWebLogic() throws Exception {
        try (GraphContext context = factory.create(true)) {
            startWindup(WEBLOGIC_TEST_EJB_XMLS, context);
            EnvironmentReferenceService envRefService = new EnvironmentReferenceService(context);
            GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(context, EjbSessionBeanModel.class);
            EjbSessionBeanModel exampleService = ejbSessionBeanService.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, "WindupAnotherExampleService");

            Assert.assertEquals(exampleService.getGlobalJndiReference().getJndiLocation(), "session/service/WindupAnotherExampleServiceLocalHome");
            Assert.assertEquals(exampleService.getLocalJndiReference().getJndiLocation(), "ejb/WindupAnotherExampleServiceLocalHome");
            GraphService<JNDIResourceModel> jndiResources = new GraphService<>(context, JNDIResourceModel.class);
            int jndiCount = 0;
            Map<String, String> jndiHandler = new HashMap<>();

            testEjbSessionBeanTimeout(context, "WindupExampleService", "*", 3600);
            testMdbThreadPool(context, "WindupMLBean", "WindupMLBean-ThreadPool", 5, 2);

            int returnedJNDI = testResourceRef(context);

            Set<String> clusteredEjbNames = new HashSet<>();
            clusteredEjbNames.add("WindupExampleService");
            clusteredEjbNames.add("WindupAnotherExampleService");
            clusteredEjbNames.add("WindupStatefulClusteredService");
            testClusterdEjb(context, clusteredEjbNames);

            Assert.assertEquals("Directory " + WEBLOGIC_TEST_EJB_XMLS + " didn't register expected number of JNDIs for EJBs.", 14, returnedJNDI);
        }
    }

    private void testClusterdEjb(GraphContext context, Set<String> ejbNames) {
        GraphService<EjbSessionBeanModel> ejbService = new GraphService<>(context, EjbSessionBeanModel.class);
        for (EjbSessionBeanModel sessionBean : ejbService.findAll()) {
            if (sessionBean.isClustered() != null && sessionBean.isClustered()) {
                Assert.assertTrue("EJB: [" + sessionBean.getBeanName() + "] is not expected to be clustered.",
                        ejbNames.remove(sessionBean.getBeanName()));
            }
        }

        if (!ejbNames.isEmpty()) {
            String results = StringUtils.join(ejbNames, ", ");
            Assert.fail("EJB(s) should be clustered but aren't: [" + results + "]");
        }

    }

    @Test
    public void testEJBWebSphere() throws Exception {
        try (GraphContext context = factory.create(true)) {
            startWindup(WEBSPHERE_TEST_EJB_XMLS, context);
            EnvironmentReferenceService envRefService = new EnvironmentReferenceService(context);
            GraphService<JNDIResourceModel> jndiResources = new GraphService<>(context, JNDIResourceModel.class);
            int jndiCount = 0;
            Map<String, String> jndiHandler = new HashMap<>();
            int returnedJNDI = testResourceRef(context);
            Assert.assertEquals("Directory " + WEBSPHERE_TEST_EJB_XMLS + " didn't register expected number of JNDIs for EJBs.", 9, returnedJNDI);
        }
    }

    @Test
    public void testEJBOrion() throws Exception {
        try (GraphContext context = factory.create(true)) {
            startWindup(ORION_TEST_EJB_XMLS, context);
            GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(context, EjbSessionBeanModel.class);

            // test that session beans have set JNDI by <<session-deployment>
            EjbSessionBeanModel exampleService = ejbSessionBeanService.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME,
                    "WindupExampleService");
            Assert.assertEquals(exampleService.getGlobalJndiReference().getJndiLocation(), "session/service/WindupExampleServiceLocalHome");

            EjbSessionBeanModel anotherExampleService = ejbSessionBeanService.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME,
                    "WindupAnotherExampleService");
            Assert.assertEquals(anotherExampleService.getGlobalJndiReference().getJndiLocation(),
                    "session/service/WindupAnotherExampleServiceLocalHome");

            // test <message-driven-deployment>
            GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(context, EjbMessageDrivenModel.class);
            EjbMessageDrivenModel mdb = mdbService.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, "WindupMLBean");
            Assert.assertEquals("Message driven bean destination was not loaded correctly for Orion.", mdb.getDestination().getJndiLocation(),
                    "queue/WindupMLQueue");

            // test <resource-ref-mapping>
            int foundJndi = testResourceRef(context);

            Assert.assertEquals("Directory " + ORION_TEST_EJB_XMLS + " didn't register expected number of JNDIs for EJBs.", 8, foundJndi);

            TechnologyTagService technologyTagService = new TechnologyTagService(context);
            List<TechnologyTagModel> technologyTagModels = technologyTagService.findAll();
            Assert.assertEquals("size", 2, technologyTagModels.size());
            AtomicBoolean foundEjbJar = new AtomicBoolean(false);
            AtomicBoolean foundOrionEjbJar = new AtomicBoolean(false);
            technologyTagModels.forEach(technologyTagModel -> {
                if ("EJB XML".equals(technologyTagModel.getName()) && "2.0".equals(technologyTagModel.getVersion())) foundEjbJar.set(true);
                if ("Orion EJB XML".equals(technologyTagModel.getName()) && technologyTagModel.getVersion() == null) foundOrionEjbJar.set(true);
            });
            Assert.assertTrue("Not found ejb-jar.xml", foundEjbJar.get());
            Assert.assertTrue("Not found orion-ejb-jar.xml", foundOrionEjbJar.get());
        }
    }

    @Test
    public void testEJBJBoss() throws Exception {
        try (GraphContext context = factory.create(true)) {
            startWindup(JBOSS_TEST_EJB_XMLS, context);
            GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(context, EjbSessionBeanModel.class);

            //test <message-driven>
            GraphService<EjbMessageDrivenModel> mdbService = new GraphService<>(context, EjbMessageDrivenModel.class);
            EjbMessageDrivenModel mdb = mdbService.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, "WindupMLBean");
            Assert.assertEquals("Message driven bean destination was not loaded correctly for JBoss.", mdb.getDestination().getJndiLocation(),
                    "queue/WindupMLQueue");

            Set<String> clusteredEjbNames = new HashSet<>();
            clusteredEjbNames.add("WindupExampleService");
            clusteredEjbNames.add("WindupAnotherExampleService");
            clusteredEjbNames.add("WindupStatefulClusteredService");
            testClusterdEjb(context, clusteredEjbNames);

            testEjbSessionBeanTimeout(context, "WindupExampleService", "*", 3600);

            // test <resource-ref-mapping>
            int foundJndi = testResourceRef(context);

            Assert.assertEquals("Directory " + JBOSS_TEST_EJB_XMLS + " didn't register expected number of JNDIs for EJBs.", 14, foundJndi);
            int msgDrivenFound = 0;
        }
    }

    private int testResourceRef(GraphContext context, Map<String, String> jndiToEnv) {
        GraphService<JNDIResourceModel> jndiResources = new GraphService<>(context, JNDIResourceModel.class);
        EnvironmentReferenceService envRefService = new EnvironmentReferenceService(context);
        int jndiCount = 0;
        for (JNDIResourceModel jndiResourceModel : jndiResources.findAll()) {
            if (jndiToEnv.containsKey(jndiResourceModel.getJndiLocation())) {
                String envRef = jndiToEnv.get(jndiResourceModel.getJndiLocation());
                int envCount = 0;
                for (EnvironmentReferenceModel environmentReferenceModel : envRefService
                        .findAllByProperty(EnvironmentReferenceModel.NAME, envRef)) {
                    Assert.assertNotNull(environmentReferenceModel.getJndiReference());
                    Assert.assertEquals(environmentReferenceModel.getJndiReference().getJndiLocation(), jndiResourceModel.getJndiLocation());
                    envCount++;
                }
                Assert.assertTrue(envCount > 0);
            }
            jndiCount++;
        }
        return jndiCount;
    }

    /**
     * Tests that the jndiHandlers are correctly mapped to environment resources and returns the number of jndi Handlers registered
     *
     * @param context
     * @param jndiHandler
     * @return
     */
    private int testResourceRef(GraphContext context) {
        Map<String, String> jndiHandler = new HashMap<>();
        jndiHandler.put("/WindupMail", "smtp/WindupMail");
        jndiHandler.put("jdbc/WindupDS", "jdbc/WindupDataSource");
        jndiHandler.put("/ConnectionFactory", "jms/WindupTopicConnectionFactory");
        return testResourceRef(context, jndiHandler);
    }

    private void testEjbSessionBeanTimeout(GraphContext context, String ejbName, String patternExpected, Integer timeoutInSecondsExpected) {
        GraphService<EjbSessionBeanModel> service = new GraphService<>(context, EjbSessionBeanModel.class);
        boolean found = false;

        EjbSessionBeanModel result = service.getUniqueByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName);
        if (result != null) {
            if (result.getTxTimeouts().containsKey(patternExpected)) {
                Integer val = result.getTxTimeouts().get(patternExpected);
                found = true;
                Assert.assertTrue("For EJB: [" + ejbName + "] with pattern: [" + patternExpected + "] expected EJB timeout: [" + timeoutInSecondsExpected + "] actually [" + val + "]", timeoutInSecondsExpected.equals(val));
            }
        }
        Assert.assertTrue("Expected EJB: [" + ejbName + "] with pattern: [" + patternExpected + "] and timeout [" + timeoutInSecondsExpected + "]", found);
    }

    private void testMdbSessionBeanTimeout(GraphContext context, String mdbName, String patternExpected, Integer timeoutInSecondsExpected) {
        GraphService<EjbMessageDrivenModel> service = new GraphService<>(context, EjbMessageDrivenModel.class);

        boolean found = false;

        EjbMessageDrivenModel result = service.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, mdbName);
        if (result != null) {
            if (result.getTxTimeouts().containsKey(patternExpected)) {
                Integer val = result.getTxTimeouts().get(patternExpected);
                found = true;
                Assert.assertTrue("For MDB: [" + mdbName + "] with pattern: [" + patternExpected + "] expected EJB timeout: [" + timeoutInSecondsExpected + "] actually [" + val + "]", timeoutInSecondsExpected.equals(val));
            }
        }
        Assert.assertTrue("Expected MDB: [" + mdbName + "] with pattern: [" + patternExpected + "] and timeout [" + timeoutInSecondsExpected + "]",
                found);

    }

    private void startWindup(String xmlFilePath, GraphContext context) throws IOException {
        ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
        pm.setName("Main Project");
        FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
        inputPath.setFilePath(xmlFilePath);

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        pm.addFileModel(inputPath);
        pm.setRootFileModel(inputPath);
        WindupConfiguration windupConfiguration = new WindupConfiguration()
                .setGraphContext(context);
        windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
        windupConfiguration.setOutputDirectory(outputPath);
        processor.execute(windupConfiguration);
    }


    private void testEjbSessionBeanThreadPool(GraphContext context, String ejbName, String threadPoolName, Integer maxPoolSize, Integer minPoolSize) {
        GraphService<EjbSessionBeanModel> service = new GraphService<>(context, EjbSessionBeanModel.class);
        boolean found = false;

        EjbSessionBeanModel result = service.getUniqueByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, ejbName);
        if (result != null) {
            if (result.getThreadPool() != null) {
                found = true;
                Assert.assertTrue("For EJB: [" + ejbName + "] with pool name: [" + threadPoolName + "] max: [" + maxPoolSize + "] min: [" + minPoolSize + "], actual name: [" + result.getThreadPool().getPoolName() + "] max: [" + result.getThreadPool().getMaxPoolSize() + "] min: [" + result.getThreadPool().getMinPoolSize() + "]", maxPoolSize.equals(result.getThreadPool().getMaxPoolSize()) && minPoolSize.equals(result.getThreadPool().getMinPoolSize()) && threadPoolName.equals(result.getThreadPool().getPoolName()));
            }
        }
        Assert.assertTrue("For EJB: [" + ejbName + "] with name: [" + threadPoolName + "] max: [" + maxPoolSize + "] min: [" + minPoolSize + "]", found);
    }

    private void testMdbThreadPool(GraphContext context, String mdbName, String threadPoolName, Integer maxPoolSize, Integer minPoolSize) {
        GraphService<EjbMessageDrivenModel> service = new GraphService<>(context, EjbMessageDrivenModel.class);

        boolean found = false;

        EjbMessageDrivenModel result = service.getUniqueByProperty(EjbMessageDrivenModel.EJB_BEAN_NAME, mdbName);
        if (result != null) {
            if (result.getThreadPool() != null) {
                found = true;
                Assert.assertTrue("For MDB: [" + mdbName + "] with pool name: [" + threadPoolName + "] max: [" + maxPoolSize + "] min: [" + minPoolSize + "], actual name: [" + result.getThreadPool().getPoolName() + "] max: [" + result.getThreadPool().getMaxPoolSize() + "] min: [" + result.getThreadPool().getMinPoolSize() + "]", maxPoolSize.equals(result.getThreadPool().getMaxPoolSize()) && minPoolSize.equals(result.getThreadPool().getMinPoolSize()) && threadPoolName.equals(result.getThreadPool().getPoolName()));
            }
        }
        Assert.assertTrue("For MDB: [" + mdbName + "] with name: [" + threadPoolName + "] max: [" + maxPoolSize + "] min: [" + minPoolSize + "]", found);
    }
}
