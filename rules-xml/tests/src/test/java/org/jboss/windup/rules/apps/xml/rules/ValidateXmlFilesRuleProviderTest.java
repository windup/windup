package org.jboss.windup.rules.apps.xml.rules;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.apps.xml.ValidateXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.jboss.windup.util.Iterables;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Tests the {@link ValidateXmlFilesRuleProvider}
 */
@RunWith(Arquillian.class)
public class ValidateXmlFilesRuleProviderTest
{

    public static String NOT_VALID_XML = "src/test/resources/not-xsd-valid.xml";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML();

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testValidateXmlFilesRuleProvider() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addNotValidXmlFileModel(context);
            List<? extends RuleProvider> ruleProviders = Collections.singletonList(new ValidateXmlFilesRuleProvider());
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);
            checkNotValidClassification(context);

        }
    }

    private void addNotValidXmlFileModel(GraphContext context) throws IOException
    {
        FileModel notValidXml = context.getFramed().addVertex(null, XmlFileModel.class);
        notValidXml.setFilePath(NOT_VALID_XML);
        notValidXml.setFileName("not-xsd-valid.xml");
    }

    private void checkNotValidClassification(GraphContext context) {
        GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);
        Iterable<ClassificationModel> classifications = classificationService.findAll();
        Assert.assertEquals(1, Iterables.size(classifications));
        final ClassificationModel notValidClassification = classifications.iterator().next();
        Assert.assertEquals(XmlFileModel.NOT_VALID_XML,notValidClassification.getClassification());
        Assert.assertEquals(0,notValidClassification.getEffort());
    }
}
