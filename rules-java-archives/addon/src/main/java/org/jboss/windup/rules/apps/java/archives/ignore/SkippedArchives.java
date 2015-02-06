package org.jboss.windup.rules.apps.java.archives.ignore;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.versions.EmptyVersionRange;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.SingleVersionRange;
import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.util.exception.WindupException;

/**
 * A service class keeping the set of skipped archives and handling their lookup.
 *
 * Data file format: GROUP_ID:ARTIFACT_ID:VERSION_OR_RANGE[:CLASSIFIER]
 *
 * Examples: org.apache.commons.*:*:* org.hibernate.*:hibernate-core:(3.2,4.0]:jar
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SkippedArchives
{
    private static final Map<Coordinate, VersionRange> map = new HashMap<>();

    /**
     * Load the given configuration file.
     */
    public static void load(File file)
    {
        try(FileInputStream inputStream = new FileInputStream(file))
        {
            LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
            while (it.hasNext())
            {
                String line = it.next();
                if (!line.startsWith("#") && !line.trim().isEmpty())
                {
                    add(line);
                }
            }
        }
        catch (Exception e)
        {
            throw new WindupException("Failed loading archive ignore patterns from [" + file.toString() + "]", e);
        }
    }

    public static void add(String line)
    {
        Assert.notNull(line, "Archive coordinate pattern must not be null.");
        CoordinatePattern pattern = fromCoordinatePattern(line);
        map.put(pattern.getCoordinate(), pattern.getVersion());
    }

    static CoordinatePattern fromCoordinatePattern(String coordinates)
    {
        String[] parts = coordinates.split("\\s*:\\s*");
        if (parts.length < 3)
            throw new IllegalArgumentException("Expected GAV definition format is 'GROUP_ID:ARTIFACT_ID:VERSION_OR_RANGE[:CLASSIFIER]', was: "
                        + coordinates);

        CoordinateBuilder coordinate = CoordinateBuilder.create()
                    .setGroupId(parts[0])
                    .setArtifactId(parts[1]);

        VersionRange version = null;

        if (parts[2].matches("\\*"))
            version = new EmptyVersionRange();
        else if (parts[2].matches("^(\\[|\\()[^,]+(,?[^,]+)+(\\]|\\))$"))
            version = Versions.parseMultipleVersionRange(parts[2]);
        else
            version = new SingleVersionRange(new SingleVersion(parts[2]));

        if (parts.length >= 4)
            coordinate.setClassifier(parts[3]);

        return new CoordinatePattern(coordinate, version);
    }

    public static boolean isSkipped(ArchiveCoordinateModel coordinate)
    {
        return isSkipped(CoordinateBuilder.create()
                    .setArtifactId(coordinate.getArtifactId())
                    .setGroupId(coordinate.getGroupId())
                    .setClassifier(coordinate.getClassifier())
                    .setVersion(coordinate.getVersion()));
    }

    /*
     * Public for testing purposes
     */
    public static boolean isSkipped(Coordinate coordinate)
    {
        for (Entry<Coordinate, VersionRange> entry : map.entrySet())
        {
            Coordinate pattern = entry.getKey();
            VersionRange range = entry.getValue();

            if (isPatternMatch(pattern.getGroupId(), coordinate.getGroupId())
                        && isPatternMatch(pattern.getArtifactId(), coordinate.getArtifactId())
                        && isPatternMatch(pattern.getClassifier(), coordinate.getClassifier()))
            {
                if (range.isEmpty() || range.includes(new SingleVersion(coordinate.getVersion())))
                    return true;
            }
        }
        return false;
    }

    private static boolean isPatternMatch(String pattern, String value)
    {
        if ("*".equals(pattern))
            return true;
        
        if(pattern == value)
            return true;

        if (pattern != null && pattern.equals(value))
            return true;

        if (pattern != null && pattern.endsWith("*"))
            if (value != null && value.startsWith(pattern.substring(0, pattern.length() - 1)))
                return true;

        return false;
    }

    /*
     * Public for testing purposes.
     */
    public static int getCount()
    {
        return map.size();
    }

    private static class CoordinatePattern
    {

        private CoordinateBuilder coordinate;
        private VersionRange version;

        public CoordinatePattern(CoordinateBuilder coordinate, VersionRange version)
        {
            this.coordinate = coordinate;
            this.version = version;
        }

        public CoordinateBuilder getCoordinate()
        {
            return coordinate;
        }

        public VersionRange getVersion()
        {
            return version;
        }

    }
}
