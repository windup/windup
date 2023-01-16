package org.jboss.windup.rules.apps.javaee.rules;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DiscoverEjbConfigurationXmlRuleProviderTest extends AbstractTest {

    @Inject
    private GraphContextFactory factory;

    @Inject
    private DiscoverEjbConfigurationXmlRuleProvider discoverEjbConfigurationXmlRuleProvider;

    @Test
    public void testEjbDiscoveryFindByClass() throws Exception {
        try (GraphContext context = factory.create(true)) {
            GraphService<JavaClassModel> javaClassService = new GraphService<>(context, JavaClassModel.class);
            List<JavaClassModel> classModels = new ArrayList<>();
            for (int i = 1; i < 5; i++) {
                JavaClassModel classModel = javaClassService.create();
                classModel.setQualifiedName("com.example.Foo" + i);
                classModels.add(classModel);
            }

            GraphService<EjbSessionBeanModel> ejbService = new GraphService<>(context, EjbSessionBeanModel.class);


            EjbSessionBeanModel session1 = ejbService.create();
            session1.setEjbLocal(classModels.get(0));

            EjbSessionBeanModel session2 = ejbService.create();
            session2.setEjbRemote(classModels.get(1));


            List<EjbSessionBeanModel> session1List = discoverEjbConfigurationXmlRuleProvider.findByClass(context, EjbSessionBeanModel.EJB_LOCAL, classModels.get(0), EjbSessionBeanModel.class);
            Assert.assertEquals(1, session1List.size());
            Assert.assertNotNull(session1List.get(0));
            Assert.assertNotNull(session1List.get(0).getEjbLocal());
            Assert.assertEquals(classModels.get(0).getQualifiedName(), session1List.get(0).getEjbLocal().getQualifiedName());

            List<EjbSessionBeanModel> session2List = discoverEjbConfigurationXmlRuleProvider.findByClass(context, EjbSessionBeanModel.EJB_REMOTE, classModels.get(1), EjbSessionBeanModel.class);
            Assert.assertEquals(1, session2List.size());
            Assert.assertNotNull(session2List.get(0));
            Assert.assertNotNull(session2List.get(0).getEjbRemote());
            Assert.assertEquals(classModels.get(1).getQualifiedName(), session2List.get(0).getEjbRemote().getQualifiedName());


            List<EjbSessionBeanModel> session3List = discoverEjbConfigurationXmlRuleProvider.findByClass(context, EjbSessionBeanModel.EJB_REMOTE, classModels.get(2), EjbSessionBeanModel.class);
            Assert.assertEquals(0, session3List.size());

            List<EjbSessionBeanModel> session4List = discoverEjbConfigurationXmlRuleProvider.findByClass(context, EjbSessionBeanModel.EJB_REMOTE, classModels.get(3), EjbSessionBeanModel.class);
            Assert.assertEquals(0, session4List.size());
        }
    }

    @Test
    public void testEJBMessageDrivenNotInEJBXML() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/ejb/mdb/implements";
            executeAnalysis(context, inputPath);

            GraphService<EjbMessageDrivenModel> ejbMessageDrivenService = new GraphService<>(context, EjbMessageDrivenModel.class);
            int entitiesFound = 0;
            for (EjbMessageDrivenModel messageDrivenModel : ejbMessageDrivenService.findAll()) {
                Assert.assertEquals("EJBMessageDrivenNotInEJBXML", messageDrivenModel.getEjbClass().getClassName());
                entitiesFound++;
            }
            Assert.assertEquals(1, entitiesFound);
        }
    }

    @Test
    public void testEJBSessionBeanNotInEJBXML() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/ejb/session";
            executeAnalysis(context, inputPath);

            GraphService<EjbSessionBeanModel> ejbSessionService = new GraphService<>(context, EjbSessionBeanModel.class);
            boolean homeFound = false;
            boolean remoteFound = false;
            boolean sessionBeanFound = false;
            boolean localHomeFound = false;
            boolean localObjectFound = false;
            for (EjbSessionBeanModel sessionBeanModel : ejbSessionService.findAll()) {
                if (sessionBeanModel.getEjbHome() != null && sessionBeanModel.getEjbHome().getQualifiedName().equals("EJB2SessionHomeNotInEJBXML"))
                    homeFound = true;
                if (sessionBeanModel.getEjbRemote() != null && sessionBeanModel.getEjbRemote().getQualifiedName().equals("EJB2RemoteInterfaceNotInEJBXML"))
                    remoteFound = true;
                if (sessionBeanModel.getEjbClass() != null && sessionBeanModel.getEjbClass().getQualifiedName().equals("EJB2SessionBeanNotInEJBXML"))
                    sessionBeanFound = true;
                if (sessionBeanModel.getEjbLocalHome() != null && sessionBeanModel.getEjbLocalHome().getQualifiedName().equals("EJBLocalHomeNotInEJBXML"))
                    localHomeFound = true;
                if (sessionBeanModel.getEjbLocal() != null && sessionBeanModel.getEjbLocal().getQualifiedName().equals("EJBLocalObjectNotInEJBXML"))
                    localObjectFound = true;
            }
            Assert.assertTrue(homeFound);
            Assert.assertTrue(remoteFound);
            Assert.assertTrue(sessionBeanFound);
            Assert.assertTrue(localHomeFound);
            Assert.assertTrue(localObjectFound);
        }
    }

    @Test
    public void testEJBEntityBeanNotInEJBXML() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/ejb/entity";
            executeAnalysis(context, inputPath);

            GraphService<EjbEntityBeanModel> ejbEntityService = new GraphService<>(context, EjbEntityBeanModel.class);
            int entitiesFound = 0;
            for (EjbEntityBeanModel entityBeanModel : ejbEntityService.findAll()) {
                Assert.assertEquals("EJB2EntityNotInEJBXML", entityBeanModel.getEjbClass().getClassName());
                entitiesFound++;
            }
            Assert.assertEquals(1, entitiesFound);
        }
    }

    @Test
    public void testEJBMetadataExtraction() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/";
            executeAnalysis(context, inputPath);

            GraphService<EjbMessageDrivenModel> messageDrivenService = new GraphService<>(context, EjbMessageDrivenModel.class);
            int msgDrivenFound = 0;
            for (EjbMessageDrivenModel msgDriven : messageDrivenService.findAll()) {
                if (msgDriven.getDestination() == null)
                    continue;
                Assert.assertEquals("ChatBeanDestination", msgDriven.getDestination().getJndiLocation());
                msgDrivenFound++;
            }
            Assert.assertEquals(1, msgDrivenFound);
        }
    }


}
