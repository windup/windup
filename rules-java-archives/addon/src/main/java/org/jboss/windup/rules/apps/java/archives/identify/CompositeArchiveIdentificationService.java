package org.jboss.windup.rules.apps.java.archives.identify;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.forge.addon.dependencies.Coordinate;

/**
 * A {@link ArchiveIdentificationService} that delegates to one or more provided {@link ArchiveIdentificationService} instances.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Singleton
public class CompositeArchiveIdentificationService implements ArchiveIdentificationService {
    private Set<ArchiveIdentificationService> identifiers = new LinkedHashSet<>();

    /**
     * Create a new {@link CompositeArchiveIdentificationService} instance.
     */
    public CompositeArchiveIdentificationService() {
    }

    /**
     * Create a new {@link CompositeArchiveIdentificationService} that delegates to the initial set of provided
     * {@link ArchiveIdentificationService} instances.
     */
    public CompositeArchiveIdentificationService(ArchiveIdentificationService... identifiers) {
        for (ArchiveIdentificationService identifier : identifiers) {
            addIdentifier(identifier);
        }
    }

    @Override
    public Coordinate getCoordinate(String checksum) {
        for (ArchiveIdentificationService identifier : identifiers) {
            Coordinate coordinate = identifier.getCoordinate(checksum);
            if (coordinate != null)
                return coordinate;
        }
        return null;
    }

    /**
     * Add a {@link ArchiveIdentificationService} instance to this {@link CompositeArchiveIdentificationService}.
     */
    public CompositeArchiveIdentificationService addIdentifier(ArchiveIdentificationService identifier) {
        this.identifiers.add(identifier);
        return this;
    }

}
