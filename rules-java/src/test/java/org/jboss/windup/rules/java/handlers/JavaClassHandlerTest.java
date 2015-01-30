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
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class JavaClassHandlerTest
{

    private static final String JAVA_CLASS_XML_FILE = "src/test/resources/handler/javaclass.windup.xml";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(JavaClassHandlerTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testJavaClassCondition() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(JAVA_CLASS_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> javaClassList = $(doc).children("javaclass").get();

        Element firstJavaClass = javaClassList.get(0);
        JavaClass javaClassCondition = parser.<JavaClass> processElement(firstJavaClass);

        Assert.assertEquals("testVariable", javaClassCondition.getVarname());
        Assert.assertEquals(null, javaClassCondition.getInputVariablesName());
        Assert.assertEquals(1, javaClassCondition.getLocations().size());
        List<TypeReferenceLocation> locations = javaClassCondition.getLocations();
        Assert.assertEquals("METHOD_CALL", locations.get(0).name());
        Assert.assertEquals("org.apache.commons.{*}", javaClassCondition.getReferences().toString());
        Assert.assertEquals("{*}File1", javaClassCondition.getTypeFilterRegex().toString());

        Element secondJavaClass = javaClassList.get(1);
        javaClassCondition = parser.<JavaClass> processElement(secondJavaClass);

        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, javaClassCondition.getVarname());
        Assert.assertEquals(3, javaClassCondition.getLocations().size());
        locations = javaClassCondition.getLocations();
        Assert.assertEquals("IMPORT", locations.get(0).name());
        Assert.assertEquals("METHOD_CALL", locations.get(1).name());
        Assert.assertEquals("INHERITANCE", locations.get(2).name());
        Assert.assertEquals("org.apache.commons.{*}", javaClassCondition.getReferences().toString());
        Assert.assertEquals(null, javaClassCondition.getTypeFilterRegex());
    }

    @Test(expected = WindupException.class)
    public void testXmlFileWithoutPublidIdAndXpath() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(JAVA_CLASS_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> javaClassList = $(doc).children("javaclass").get();

        Element firstJavaClass = javaClassList.get(2);
        JavaClass javaClassCondition = parser.<JavaClass> processElement(firstJavaClass);

    }
}