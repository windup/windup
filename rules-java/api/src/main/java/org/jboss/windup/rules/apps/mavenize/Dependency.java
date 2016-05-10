package org.jboss.windup.rules.apps.mavenize;

/**
 * Dependency reference can be anything that can appear in pom.xml's dependencies:
 * A library, project cross-module dependency, another app (EAR, WAR), etc.
 * This is a shared interface for implementation classes of those.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public interface Dependency
{
    MavenCoord getMavenCoord();

    Role getRole();

    /**
     * Whether the target artifact is internal project's module, third-party library,
     * or an API definitions for the services provided by the  application server.
     */
    enum Role {
        /**
         * A known library bundled with the project; typically opensource from Maven Central.
         */
        LIBRARY,

        /**
         * A reference to another module of a project; typically used for inter-project cross-references.
         */
        MODULE,

        /**
         * API dependency, typically not bundled, rather provided by an application server.
         */
        API
    }

}
