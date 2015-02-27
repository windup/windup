package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;

import org.jboss.forge.addon.dependencies.Coordinate;

/**
 * Used to map {@link File} checksums (SHA1, MD5, etc) to {@link Coordinate} instances.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface ChecksumIdentifier
{
    /**
     * Given a checksum of a {@link File}, and given that checksum can be identified as a known {@link Coordinate},
     * return the {@link Coordinate} for the given checksum. (May be <code>null</code>.)
     */
    public Coordinate getCoordinate(String checksum);
}
