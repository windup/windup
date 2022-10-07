package org.jboss.windup.rules.apps.xml.rules;

import com.google.common.collect.Iterables;
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
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.xml.ValidateXmlFilesRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests the {@link ValidateXmlFilesRuleProvider} and simulates good internet access with mocks
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RunWith(Arquillian.class)
public class ValidateXmlFilesRuleProviderWithInternetTest extends AbstractXsdValidationTest {
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
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addClass(AbstractXsdValidationTest.class)
                .addBeansXML();
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private ValidateXmlFilesRuleProvider validateXmlRuleProvider;

    @Test
    public void testNotValidXml() throws Exception {
        try (GraphContext context = factory.create(true)) {
            initOnlineWindupConfiguration(context);
            addFileModel(context, NOT_VALID_XML);

            List<? extends RuleProvider> ruleProviders = Arrays.asList(validateXmlRuleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(1, Iterables.size(classifications));

            final ClassificationModel notValidClassification = classifications.iterator().next();
            Assert.assertEquals(XmlFileModel.NOT_VALID_XML, notValidClassification.getClassification());
            Assert.assertEquals(0, notValidClassification.getEffort());
            Assert.assertEquals(1, Iterables.size(notValidClassification.getFileModels()));
            FileModel fileWithClassification = notValidClassification.getFileModels().iterator().next();
            Assert.assertEquals(parseFileName(NOT_VALID_XML), fileWithClassification.getFileName());
        }
    }

    @Test
    public void testNotValidXsdUrlAttribute() throws Exception {
        try (GraphContext context = factory.create(true)) {
            initOnlineWindupConfiguration(context);
            addFileModel(context, NOT_VALID_XSD_SCHEMA_URL);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(validateXmlRuleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(1, Iterables.size(classifications));

            InlineHintService hintService = new InlineHintService(context);
            Iterable<InlineHintModel> hints = hintService.findAll();
            Assert.assertEquals(2, Iterables.size(hints));
            for (InlineHintModel hint : hints) {
                switch (hint.getTitle()) {
                    case XmlFileModel.XSD_URL_NOT_VALID:
                        Assert.assertEquals(XmlFileModel.XSD_URL_NOT_VALID, hint.getTitle());
                        Assert.assertEquals(1, hint.getEffort());
                        Assert.assertEquals(parseFileName(NOT_VALID_XSD_SCHEMA_URL), hint.getFile().getFileName());
                        break;
                    case "XML File is not valid":
                        break;
                    default:
                        Assert.fail("Unrecognized hint: " + hint.getTitle());
                }
            }
        }
    }

    @Test
    public void testWithoutXsdUrlAttribute() throws Exception {
        try (GraphContext context = factory.create(true)) {
            addFileModel(context, NO_XSD_SCHEMA_URL);
            initOnlineWindupConfiguration(context);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(validateXmlRuleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(0, Iterables.size(classifications));
        }
    }

    @Test
    @Ignore // Ignoring for now as we are not currently running validation in offline mode
    public void testNotValidXmlInOfflineMode() throws Exception {
        try (GraphContext context = factory.create(true)) {
            initOfflineWindupConfiguration(context);
            addFileModel(context, NOT_VALID_XML);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(validateXmlRuleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                    ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            Assert.assertEquals(1, Iterables.size(classifications));
        }
    }

    @Test
    public void testUnparsableUrl() throws Exception {
        try (GraphContext context = factory.create(true)) {
            initOnlineWindupConfiguration(context);
            addFileModel(context, URL_NOT_PARSABLE);

            List<? extends RuleProvider> ruleProviders = Collections.singletonList(validateXmlRuleProvider);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            GraphService<InlineHintModel> hintService = new GraphService<>(context,
                    InlineHintModel.class);
            Iterable<InlineHintModel> hints = hintService.findAll();
            Assert.assertEquals(2, Iterables.size(hints));

            for (InlineHintModel hint : hints) {
                switch (hint.getTitle()) {
                    case XmlFileModel.XSD_URL_NOT_VALID:
                        break;
                    case XmlFileModel.NOT_VALID_XML:
                        break;
                    default:
                        Assert.fail("Unrecognized hint: " + hint.getTitle());
                }
            }

        }
    }


    @Test
    public void testMultipleSchemas() throws Exception {
        try (GraphContext context = factory.create(true)) {
            initOnlineWindupConfiguration(context);
            addFileModel(context, URL_MULTIPLE_SCHEMAS);

            ValidateXmlFilesRuleProvider ruleProviderNoMocks = new ValidateXmlFilesRuleProvider();
            List<? extends RuleProvider> ruleProviders = Collections.singletonList(ruleProviderNoMocks);
            WindupTestUtilMethods.runOnlyRuleProviders(ruleProviders, context);

            Service<InlineHintModel> hintService = context.service(InlineHintModel.class);
            Iterable<InlineHintModel> hints = hintService.findAll();
            Assert.assertEquals(0, Iterables.size(hints));
        }
    }

}
