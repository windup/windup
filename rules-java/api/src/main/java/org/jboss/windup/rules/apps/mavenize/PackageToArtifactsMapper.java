package org.jboss.windup.rules.apps.mavenize;

import java.util.List;


/**
 * Maps from a package name to Artifacts containing that package.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
interface PackageToArtifactsMapper
{
    /**
     * @return All artifacts known to contain given package.
     */
    List<MavenCoord> getArtifactsContainingPackage(String pkg);
}
