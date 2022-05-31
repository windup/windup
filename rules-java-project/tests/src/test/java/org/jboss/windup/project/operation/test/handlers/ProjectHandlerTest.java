package org.jboss.windup.project.operation.test.handlers;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.jboss.windup.rules.apps.java.condition.Version;
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
public class ProjectHandlerTest {

    private static final String PROJECT_XML_FILE = "src/test/resources/xml/project.xml";
    @Inject
    private Furnace furnace;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")})
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testLineItemWithMessage() throws Exception {
        File fXmlFile = new File(PROJECT_XML_FILE);
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> projectList = $(doc).children("project").get();
        Element projectElement = projectList.get(0);
        Project project = parser.<Project>processElement(projectElement);
        Artifact artifact = project.getArtifact();
        String artifactId = artifact.getArtifactId().toString();
        String groupId = artifact.getGroupId().toString();
        Version version = artifact.getVersion();
        String from = version.getFrom();
        String to = version.getTo();
        Assert.assertEquals("someArtifactId", artifactId);
        Assert.assertEquals("someGroupId", groupId);
        Assert.assertEquals("SomeFromVersion", from);
        Assert.assertEquals("SomeToVersion", to);

    }

}
