package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.rules.apps.java.archives.identify.api.FilesBasedIdentifier;

/**
 * Used to identify JAR files. Maps SHA1 hashes to a {@link Coordinate}.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class StaticArchiveIdentifier implements FilesBasedIdentifier
{
    private static final Map<String, String> map = new TreeMap<>();

    @Override
    public Coordinate getCoordinateFromSHA1(String sha1Hash)
    {
        return IdentifiedArchives.getCoordinateFromSHA1(sha1Hash);
    }


    public void addMapping(String sha1, String coordinate)
    {
        IdentifiedArchives.addMapping(sha1, coordinate);
    }


    @Override
    public void addMappingsFrom(File file)
    {
        IdentifiedArchives.addMappingsFrom(file);
    }

}