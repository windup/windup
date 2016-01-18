package org.jboss.windup.rules.apps.xml.rules;

import com.google.common.collect.Iterables;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.xml.ValidateXmlFilesRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.validation.SchemaFactory;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ValidateXmlFilesRuleProvider} and simulates bad internet access with mocks
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RunWith(Arquillian.class)
public class ValidateXmlFilesRuleProviderWithoutInternetTest  extends AbstractXsdValidationTest
{

    private RuleProvider ruleProvider;

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
                    .addClass(Mockito.class)
                    .addClass(AbstractXsdValidationTest.class)
                    .addBeansXML();

        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Before
    public void initMocks() throws SAXException
    {
        SchemaFactory schemaFactory = Mockito.mock(SchemaFactory.class);
        when(schemaFactory.newSchema(any(URL.class))).thenThrow(new SAXException(new UnknownHostException()));
        ruleProvider=new ValidateXmlFilesRuleProvider(schemaFactory);
    }

    @Test
    public void testValidXmlWithoutClassification() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addFileModel(context, VALID_XML);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                        ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(0, Iterables.size(classifications));
        }
    }

    @Test
    public void testNotValidXml() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addFileModel(context, NOT_VALID_XML);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                        ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(0, Iterables.size(classifications));
        }
    }

    @Test
    public void testNotValidXsdUrlAttribute() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addFileModel(context, NOT_VALID_XSD_SCHEMA_URL);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                        ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(0, Iterables.size(classifications));

            InlineHintService hintService = new InlineHintService(context);
            Iterable<InlineHintModel> hints = hintService.findAll();
            Assert.assertEquals(0, Iterables.size(hints));

        }
    }

    @Test
    public void testWithoutXsdUrlAttribute() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addFileModel(context, NO_XSD_SCHEMA_URL);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                        ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(0, Iterables.size(classifications));

        }
    }

    @Test
    public void testUnparsableUrl() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            addFileModel(context, URL_NOT_PARSABLE);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<InlineHintModel> hintService = new GraphService<>(context,
                        InlineHintModel.class);
            Iterable<InlineHintModel> hints = hintService.findAll();
            Assert.assertEquals(1, Iterables.size(hints));

            final InlineHintModel hint = hints.iterator().next();
            Assert.assertEquals(XmlFileModel.XSD_URL_NOT_VALID,hint.getTitle());

        }
    }

}
