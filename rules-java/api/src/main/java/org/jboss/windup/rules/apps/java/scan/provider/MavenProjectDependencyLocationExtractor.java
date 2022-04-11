package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.graph.model.DependencyLocation;
import org.w3c.dom.Node;

/**
 * Given a Node in a Maven POM file which contains a dependency, extracts its location.
 *
 * @author <a href="mailto:jleflete@redhat.com">Juan Manuel Leflet Estrada</a>
 */
public class MavenProjectDependencyLocationExtractor {
    public MavenProjectDependencyLocationExtractor() {
    }

    DependencyLocation extractDependencyLocation(Node node) {
        if (node.getNodeName().equals("parent")) {
            return DependencyLocation.PARENT;
        } else if (node.getNodeName().equals("dependency")) {
            if (node.getParentNode().getParentNode().getNodeName().equals("dependencyManagement")) {
                return DependencyLocation.DEPENDENCY_MANAGEMENT;
            } else {
                return DependencyLocation.DEPENDENCIES;
            }
        } else {
            if (node.getParentNode().getParentNode().getNodeName().equals("pluginManagement")) {
                return DependencyLocation.PLUGIN_MANAGEMENT;
            } else {
                return DependencyLocation.PLUGINS;
            }
        }
    }
}