package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.util.exception.WindupException;

/**
 * Used to identify JAR files. Maps SHA1 hashes to a {@link Coordinate}.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class IdentifiedArchives
{
    private static final Map<String, String> map = new TreeMap<>();

    public static Coordinate getCoordinateFromSHA1(String sha1Hash)
    {
        if (sha1Hash == null)
            return null;

        String coordinate = map.get(sha1Hash);

        if (coordinate == null)
            return null;

        return CoordinateBuilder.create(coordinate);
    }

    public static void addMapping(String sha1, String coordinate)
    {
        map.put(sha1, coordinate);
    }

    public static void addMappingsFrom(File file)
    {
        try (FileInputStream inputStream = new FileInputStream(file))
        {
            LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
            while (it.hasNext())
            {
                String line = it.next();
                if (line.startsWith("#") || line.trim().isEmpty())
                    continue;
                String[] parts = StringUtils.split(line, ' ');
                if (parts.length < 2)
                    throw new IllegalArgumentException("Expected 'SHA1 GROUP_ID:ARTIFACT_ID:VERSION[:COORDINATE]', but was [" + line + "] in ["
                                + file + "]");

                addMapping(parts[0], parts[1]);
            }
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to load SHA1 to " + Coordinate.class.getSimpleName() + " definitions from [" + file + "]", e);
        }
    }
}