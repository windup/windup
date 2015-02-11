package org.jboss.windup.rules.apps.java.archives.identify.api;

import org.jboss.forge.addon.dependencies.Coordinate;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface ArchiveHashIdentifier
{
    public Coordinate getCoordinateFromSHA1(String sha1Hash);
}
