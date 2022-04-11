package org.jboss.windup.rules.apps.java.scan.provider;

import org.dom4j.dom.DOMElement;
import org.jboss.windup.graph.model.DependencyLocation;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.assertTrue;

public class MavenProjectDependencyLocationExtractorTest {

    MavenProjectDependencyLocationExtractor mavenProjectDependencyLocationExtractor = new MavenProjectDependencyLocationExtractor();

    @Test
    public void testExtractDependencyLocationInParent() {
        Node node = new DOMElement("parent");

        DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(node);

        assertTrue(dependencyLocation == DependencyLocation.PARENT);
    }

    @Test
    public void testExtractDependencyLocationInDependencyManagement() {
        Node dependencyManagementNode = new DOMElement("dependencyManagement");
        Node dependenciesNode = new DOMElement("dependencies");
        Node dependencyNode = new DOMElement("dependency");
        dependencyManagementNode.appendChild(dependenciesNode);
        dependenciesNode.appendChild(dependencyNode);

        DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(dependencyNode);

        assertTrue(dependencyLocation == DependencyLocation.DEPENDENCY_MANAGEMENT);
    }

    @Test
    public void testExtractDependencyLocationInDependencies() {
        Node projectNode = new DOMElement("project");
        Node dependenciesNode = new DOMElement("dependencies");
        Node dependencyNode = new DOMElement("dependency");
        projectNode.appendChild(dependenciesNode);
        dependenciesNode.appendChild(dependencyNode);

        DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(dependencyNode);

        assertTrue(dependencyLocation == DependencyLocation.DEPENDENCIES);
    }

    @Test
    public void testExtractDependencyLocationInPluginManagement() {
        Node pluginManagementNode = new DOMElement("pluginManagement");
        Node pluginsNode = new DOMElement("plugins");
        Node dependencyNode = new DOMElement("plugin");
        pluginManagementNode.appendChild(pluginsNode);
        pluginsNode.appendChild(dependencyNode);

        DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(dependencyNode);

        assertTrue(dependencyLocation == DependencyLocation.PLUGIN_MANAGEMENT);
    }

    @Test
    public void testExtractDependencyLocationInPlugins() {
        Node projectNode = new DOMElement("project");
        Node pluginsNode = new DOMElement("plugins");
        Node dependencyNode = new DOMElement("plugin");
        projectNode.appendChild(pluginsNode);
        pluginsNode.appendChild(dependencyNode);

        DependencyLocation dependencyLocation = mavenProjectDependencyLocationExtractor.extractDependencyLocation(dependencyNode);

        assertTrue(dependencyLocation == DependencyLocation.PLUGINS);
    }
}