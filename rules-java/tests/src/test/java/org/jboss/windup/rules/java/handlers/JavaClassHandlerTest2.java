package org.jboss.windup.rules.java.handlers;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.joox.JOOX.$;

@RunWith(Arquillian.class)
public class JavaClassHandlerTest2 {

    private static final String JAVA_CLASS_XML_WINDUP_FILE = "src/test/resources/handler/javaclass-enumconst.windup.xml";
    private static final String JAVA_CLASS_XML_RHAMT_FILE = "src/test/resources/handler/javaclass-enumconst.rhamt.xml";
    private static final String JAVA_CLASS_XML_MTA_FILE = "src/test/resources/handler/javaclass-enumconst.mta.xml";
    @Inject
    private Furnace furnace;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();

        return archive;
    }

    @Test
    public void testWindupJavaClassEnumLocationCondition() throws Exception {
        File fXmlFile = new File(JAVA_CLASS_XML_WINDUP_FILE);
        testJavaClassEnumLocationCondition(fXmlFile);
    }

    @Test
    public void testRhamtJavaClassEnumLocationCondition() throws Exception {
        File fXmlFile = new File(JAVA_CLASS_XML_RHAMT_FILE);
        testJavaClassEnumLocationCondition(fXmlFile);
    }

    @Test
    public void testMtaJavaClassEnumLocationCondition() throws Exception {
        File fXmlFile = new File(JAVA_CLASS_XML_MTA_FILE);
        testJavaClassEnumLocationCondition(fXmlFile);
    }

    public void testJavaClassEnumLocationCondition(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> javaClassList = $(doc).children("javaclass").get();

        Element firstJavaClass = javaClassList.get(0);
        JavaClass javaClassCondition = parser.<JavaClass>processElement(firstJavaClass);

        Assert.assertEquals("testVariable", javaClassCondition.getVarname());
        Assert.assertEquals(null, javaClassCondition.getInputVariablesName());
        Assert.assertEquals(1, javaClassCondition.getLocations().size());
        List<TypeReferenceLocation> locations = javaClassCondition.getLocations();
        Assert.assertEquals("ENUM_CONSTANT", locations.get(0).name());
        Assert.assertEquals("java.nio.file.AccessMode.{*}", javaClassCondition.getReferences().toString());

        Assert.assertEquals("{*}File3", javaClassCondition.getTypeFilterRegex().toString());
    }

}