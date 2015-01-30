package org.jboss.windup.project.operation.test.handlers;

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
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.jboss.windup.project.condition.Version;
import org.jboss.windup.project.operation.LineItem;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class ProjectHandlerTest
{

    private static final String PROJECT_XML_FILE = "src/test/resources/xml/project.xml";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules:rules-java-project", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(ProjectHandlerTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.rules:rules-java-project"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"));
        return archive;
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testLineItemWithMessage() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(PROJECT_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> projectList = $(doc).children("project").get();
        Element projectElement = projectList.get(0);
        Project project = parser.<Project> processElement(projectElement);
        Artifact artifact = project.getArtifact();
        String artifactId = artifact.getArtifactId();
        String groupId = artifact.getGroupId();
        Version version = artifact.getVersion();
        String from = version.getFrom();
        String to = version.getTo();
        Assert.assertEquals("someArtifactId", artifactId);
        Assert.assertEquals("someGroupId", groupId);
        Assert.assertEquals("SomeFromVersion", from);
        Assert.assertEquals("SomeToVersion", to);

    }

}