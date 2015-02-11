package org.jboss.windup.rules.apps.java.archives.identify;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.rules.apps.java.archives.identify.api.ArchiveHashIdentifier;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class CompositeIdentifier implements ArchiveHashIdentifier
{
    Set<ArchiveHashIdentifier> identifiers = new LinkedHashSet<>();

    @Override
    public Coordinate getCoordinateFromSHA1(String sha1Hash)
    {
        for (ArchiveHashIdentifier ident : identifiers)
        {
            Coordinate coord = ident.getCoordinateFromSHA1(sha1Hash);
            if (coord != null)
                return coord;
        }
        return null;
    }


    public CompositeIdentifier addIdentifier(ArchiveHashIdentifier ident)
    {
        this.identifiers.add(ident);
        return this;
    }

}// class
