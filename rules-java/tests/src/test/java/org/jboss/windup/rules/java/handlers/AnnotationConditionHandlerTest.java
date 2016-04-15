package org.jboss.windup.rules.java.handlers;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class AnnotationConditionHandlerTest
{

    private static final String ANNOTATION_HANDLER_XML_FILE = "src/test/resources/handler/annotationcondition.windup.xml";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();

        return archive;
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testCondition() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(ANNOTATION_HANDLER_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> javaClassList = $(doc).children("javaclass").get();

        Element javaClass1 = javaClassList.get(0);
        JavaClass javaClassCondition1 = parser.<JavaClass> processElement(javaClass1);

        String asString1 = javaClassCondition1.toString();
        Assert.assertTrue(asString1.contains("annotationConditions(AnnotationTypeCondition{pattern={*}, conditions={firstName=AnnotationListCondition{index=1, conditions=[AnnotationLiteralCondition{pattern=firstPattern}]}}})"));

        Element javaClass2 = javaClassList.get(1);
        JavaClass javaClassCondition2 = parser.<JavaClass> processElement(javaClass2);

        String asString2 = javaClassCondition2.toString();
        Assert.assertTrue(asString2.contains("AnnotationTypeCondition{pattern={*}, conditions={secondName"));
        Assert.assertTrue(asString2.contains("{subLiteral=AnnotationLiteralCondition{pattern=subLiteralPattern}"));
    }
}