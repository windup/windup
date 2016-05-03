package org.jboss.windup.rules.apps.java.archives.identify;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.util.Logging;

/**
 * A {@link ArchiveIdentificationService} that delegates to one or more provided {@link ArchiveIdentificationService} instances.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Singleton
public class CompositeArchiveIdentificationService implements ArchiveIdentificationService
{
    private static final Logger LOG = Logging.get(CompositeArchiveIdentificationService.class);

    protected final Set<ArchiveIdentificationService> identifiers = new LinkedHashSet<>();

    /**
     * Create a new {@link CompositeArchiveIdentificationService} instance.
     */
    public CompositeArchiveIdentificationService()
    {
    }

    /**
     * Create a new {@link CompositeArchiveIdentificationService} that delegates to the initial set of provided
     * {@link ArchiveIdentificationService} instances.
     */
    public CompositeArchiveIdentificationService(ArchiveIdentificationService... identifiers)
    {
        for (ArchiveIdentificationService identifier : identifiers)
        {
            addIdentifier(identifier);
        }
    }

    @Override
    public List<Coordinate> getCoordinates(String checksum)
    {
        for (ArchiveIdentificationService identifier : identifiers)
        {
            List<Coordinate> coordinates = identifier.getCoordinates(checksum);
            if (coordinates != null)
                return coordinates;
        }
        return null;
    }

    /**
     * Add a {@link ArchiveIdentificationService} instance to this {@link CompositeArchiveIdentificationService}.
     */
    public final CompositeArchiveIdentificationService addIdentifier(ArchiveIdentificationService identifier)
    {
        LOG.info("Adding identifier: " + identifier);
        this.identifiers.add(identifier);
        return this;
    }

}
