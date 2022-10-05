package org.jboss.windup.rules.apps.mavenize;

/**
 * Represents a simple dependency for the purposes of determining provided dependencies.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SimpleDependency implements Dependency {
    MavenCoord coord;
    Role role;

    /**
     * Creates an instance with the given {@link Role} and {@link MavenCoord}.
     */
    public SimpleDependency(Role role, MavenCoord coord) {
        this.coord = coord;
        this.role = role;
    }

    /**
     * Gets the Maven Coordinate associated with this dependency.
     */
    public MavenCoord getCoord() {
        return coord;
    }

    /**
     * Gets the {@link Role} that can be used to determine how to reference this dependency. For example, whether
     * or not this is part of the server's public API.
     */
    public Role getRole() {
        return role;
    }
}
