package org.jboss.windup.rules.apps.mavenize;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SimpleDependency implements Dependency
{
    MavenCoord coord;
    Role role;

    public SimpleDependency(Role role, MavenCoord coord)
    {
        this.coord = coord;
        this.role = role;
    }

    public MavenCoord getMavenCoord()
    {
        return coord;
    }

    public Role getRole()
    {
        return role;
    }
}
