package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.util.exception.WindupException;

/**
 * In-memory implementation of {@link ArchiveIdentificationService}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class InMemoryArchiveIdentificationService implements ArchiveIdentificationService {
    private final Map<String, String> map = new TreeMap<>();

    @Override
    public Coordinate getCoordinate(String checksum) {
        if (checksum == null)
            return null;

        String coordinate = map.get(checksum);

        if (coordinate == null)
            return null;

        return CoordinateBuilder.create(coordinate);
    }

    public InMemoryArchiveIdentificationService addMapping(String checksum, String coordinate) {
        map.put(checksum, coordinate);
        return this;
    }

    public InMemoryArchiveIdentificationService addMappingsFrom(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            LineIterator iterator = IOUtils.lineIterator(inputStream, "UTF-8");
            int lineNumber = 0;
            while (iterator.hasNext()) {
                lineNumber++;
                String line = iterator.next();
                if (line.startsWith("#") || line.trim().isEmpty())
                    continue;
                String[] parts = StringUtils.split(line, ' ');
                if (parts.length < 2)
                    throw new IllegalArgumentException("Expected 'SHA1 GROUP_ID:ARTIFACT_ID:[PACKAGING:[COORDINATE:]]VERSION', but was: [" + line
                            + "] in [" + file + "] at line [" + lineNumber + "]");

                addMapping(parts[0], parts[1]);
            }
        } catch (IOException e) {
            throw new WindupException("Failed to load SHA1 to " + Coordinate.class.getSimpleName() + " definitions from [" + file + "]", e);
        }
        return this;
    }

}