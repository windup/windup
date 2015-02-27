package org.jboss.windup.rules.apps.java.archives.identify;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.forge.addon.dependencies.Coordinate;

/**
 * A {@link ChecksumIdentifier} that delegates to one or more provided {@link ChecksumIdentifier} instances.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class CompositeChecksumIdentifier implements ChecksumIdentifier
{
    private Set<ChecksumIdentifier> identifiers = new LinkedHashSet<>();

    /**
     * Create a new {@link CompositeChecksumIdentifier} instance.
     */
    public CompositeChecksumIdentifier()
    {
    }

    /**
     * Create a new {@link CompositeChecksumIdentifier} that delegates to the initial set of provided
     * {@link ChecksumIdentifier} instances.
     */
    public CompositeChecksumIdentifier(ChecksumIdentifier... identifiers)
    {
        for (ChecksumIdentifier identifier : identifiers)
        {
            addIdentifier(identifier);
        }
    }

    @Override
    public Coordinate getCoordinate(String checksum)
    {
        for (ChecksumIdentifier identifier : identifiers)
        {
            Coordinate coordinate = identifier.getCoordinate(checksum);
            if (coordinate != null)
                return coordinate;
        }
        return null;
    }

    /**
     * Add a {@link ChecksumIdentifier} instance to this {@link CompositeChecksumIdentifier}.
     */
    public CompositeChecksumIdentifier addIdentifier(ChecksumIdentifier identifier)
    {
        this.identifiers.add(identifier);
        return this;
    }

}
