package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;

import org.jboss.forge.addon.dependencies.Coordinate;

/**
 * Used to map {@link File} hashes (SHA1, MD5, etc) to {@link Coordinate} instances.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public interface ArchiveIdentificationService {
    /**
     * Given a hash of a {@link File}, and given that the hash can be identified as a known {@link Coordinate}, return the {@link Coordinate} for the
     * given hash. (May be <code>null</code>.)
     */
    Coordinate getCoordinate(String hash);
}
