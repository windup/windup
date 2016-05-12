package org.jboss.windup.rules.apps.mavenize;

import org.jboss.windup.graph.model.ProjectModel;


/**
 * Adds the appropriate API dependencies to Maven POMs based on what's found in the project.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public interface DependencyDeducer
{
    void addAppropriateDependencies(ProjectModel projectModel, Pom modulePom);

}
