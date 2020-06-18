package org.jboss.windup.rules.java.handlers;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.Collections;
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
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class AnnotationConditionHandlerTest
{

    private static final String ANNOTATION_HANDLER_XML_WINDUP_FILE = "src/test/resources/handler/annotationcondition.windup.xml";
    private static final String ANNOTATION_HANDLER_XML_RHAMT_FILE = "src/test/resources/handler/annotationcondition.rhamt.xml";
    private static final String ANNOTATION_HANDLER_XML_MTA_FILE = "src/test/resources/handler/annotationcondition.mta.xml";

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
    public void testWindupCondition() throws Exception
    {
        File fXmlFile = new File(ANNOTATION_HANDLER_XML_WINDUP_FILE);
        testCondition(fXmlFile);
    }

    @Test
    public void testRhamtCondition() throws Exception
    {
        File fXmlFile = new File(ANNOTATION_HANDLER_XML_RHAMT_FILE);
        testCondition(fXmlFile);
    }

    @Test
    public void testMtaCondition() throws Exception
    {
        File fXmlFile = new File(ANNOTATION_HANDLER_XML_MTA_FILE);
        testCondition(fXmlFile);
    }

    public void testCondition(File fXmlFile) throws Exception
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> javaClassList = $(doc).children("javaclass").get();

        Element javaClass1 = javaClassList.get(0);
        JavaClass javaClassCondition1 = parser.<JavaClass> processElement(javaClass1);

        String asString1 = javaClassCondition1.toString();
        System.out.println("Condition 1: " + asString1);
        Assert.assertTrue(asString1.contains("annotationConditions(AnnotationTypeCondition{pattern={*}, conditions={firstName=AnnotationListCondition{index=1, conditions=[AnnotationLiteralCondition{pattern=firstPattern}]}}})"));

        Element javaClass2 = javaClassList.get(1);
        JavaClass javaClassCondition2 = parser.<JavaClass> processElement(javaClass2);

        String asString2 = javaClassCondition2.toString();
        System.out.println("Condition 2: " + asString2);
        Assert.assertTrue(asString2.contains("AnnotationTypeCondition{pattern={*}, conditions={secondName"));
        Assert.assertTrue(asString2.contains("{subLiteral=AnnotationLiteralCondition{pattern=subLiteralPattern}"));

        Element javaClass3 = javaClassList.get(2);
        JavaClass javaClassCondition3 = parser.<JavaClass> processElement(javaClass3);

        String asString3 = javaClassCondition3.toString();
        System.out.println("Condition 3: " + asString3);
        Assert.assertTrue(asString3.contains("AnnotationTypeCondition{pattern={*}, conditions={value=AnnotationListCondition"));
    }
}
