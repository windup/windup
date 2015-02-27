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
 * In-memory implementation of {@link ChecksumIdentifier}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class InMemoryChecksumIdentifier implements ChecksumIdentifier
{
    private final Map<String, String> map = new TreeMap<>();

    @Override
    public Coordinate getCoordinate(String checksum)
    {
        if (checksum == null)
            return null;

        String coordinate = map.get(checksum);

        if (coordinate == null)
            return null;

        return CoordinateBuilder.create(coordinate);
    }

    public void addMapping(String checksum, String coordinate)
    {
        map.put(checksum, coordinate);
    }

    public void addMappingsFrom(File file)
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
                    throw new IllegalArgumentException("Expected 'SHA1 GROUP_ID:ARTIFACT_ID:[PACKAGING:[COORDINATE:]]VERSION', but was: \n" + line
                                + "\n\tin [" + file + "]");

                addMapping(parts[0], parts[1]);
            }
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to load SHA1 to " + Coordinate.class.getSimpleName() + " definitions from [" + file + "]", e);
        }
    }

}