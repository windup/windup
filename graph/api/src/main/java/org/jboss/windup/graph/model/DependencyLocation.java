package org.jboss.windup.graph.model;

/**
 * Represents a location of a dependency within a Maven POM file.
 *
 * @author <a href="mailto:jleflete@redhat.com">Juan Manuel Leflet Estrada</a>
 */
public enum DependencyLocation {

    PARENT,

    DEPENDENCIES,

    DEPENDENCY_MANAGEMENT,

    PLUGINS,

    PLUGIN_MANAGEMENT;

}
