package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.furnace.util.Assert;

/**
 * File-based implementation of {@link ChecksumIdentifier}. Searches a sorted file with lines of this format:
 * <p>
 * 
 * <pre>
 * "SHA1_hash groupId:artifactId:version".
 * </pre>
 * 
 * Search is performed using a simple binary search algorithm. Results are cached in memory.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class SortedFileChecksumIdentifier implements ChecksumIdentifier
{
    private static final Logger log = Logger.getLogger(SortedFileChecksumIdentifier.class.getName());

    public static final int SHA1_LENGTH = 40;
    private File dataFile;

    private SortedMap<String, Long> resultLocationCache = new TreeMap<>();

    public SortedFileChecksumIdentifier(File dataFile)
    {
        Assert.isTrue(dataFile.exists(), "Hash to Coordinate data file does not exist: " + dataFile.toString());

        this.dataFile = dataFile;
    }

    @Override
    public Coordinate getCoordinate(String sha1Hash)
    {
        try
        {
            Range range = findRawSeekRange(sha1Hash);
            String gavRawData = lookForEntry_PivotSearch(sha1Hash, range);
            if (null == gavRawData)
                return null;

            gavRawData = StringUtils.substring(gavRawData, SHA1_LENGTH).trim();
            return CoordinateBuilder.create(gavRawData);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Could not find SHA1 to G:A:V entries: " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns the range in the file where to look for given hash. This is basically a binary search in a cache, which
     * is an ordered Map: SHA1 hash -> position in the file (assuming the file is ordered!).
     */
    private Range findRawSeekRange(String sha1Hash)
    {
        if (this.resultLocationCache.size() == 0)
            return new Range(0, this.dataFile.length());

        sha1Hash = sha1Hash.toLowerCase();

        Set<String> keySet = this.resultLocationCache.keySet();
        List<String> keys = new ArrayList<>(keySet);

        Range cacheIndexRange = new Range(0, this.resultLocationCache.size() - 1);
        Range fileSeekRange = new Range(this.resultLocationCache.get(this.resultLocationCache.firstKey()),
                    this.resultLocationCache.get(this.resultLocationCache.lastKey()));

        while (cacheIndexRange.diff() > 1)
        {
            long pivot = (cacheIndexRange.a + cacheIndexRange.b) / 2;
            final String hashAtPivot = keys.get((int) pivot);
            int comp = sha1Hash.compareTo(hashAtPivot);
            if (comp == 0) // The exact hash. What a chance.
            {
                final Long offsetAtPivot = this.resultLocationCache.get(hashAtPivot);
                return fileSeekRange.set(offsetAtPivot, offsetAtPivot);
            }
            if (comp < 0)
                cacheIndexRange.b = pivot;
            else
                cacheIndexRange.a = pivot;
        }

        return new Range(this.resultLocationCache.get(keys.get((int) cacheIndexRange.a)), this.resultLocationCache.get(keys
                    .get((int) cacheIndexRange.b)));
    }

    /**
     * @return A single entry for given hash in this SortedIdentifier's file within the given seek range, or null if not
     *         found.
     */
    private String lookForEntry_PivotSearch(String sha1Hash, Range range) throws FileNotFoundException, IOException
    {
        if (log.isLoggable(Level.FINER))
            log.finer("Looking for " + sha1Hash);

        final byte[] hashBytes = sha1Hash.getBytes();

        try (RandomAccessFile seekable = new RandomAccessFile(this.dataFile, "r"))
        {
            final byte[] entryHashBytes = new byte[SHA1_LENGTH];

            do
            {
                if (range.diff() < 300) // The longest entry length estimation * 2.
                    return lookForEntry_LinearSearch(sha1Hash, seekable, range);

                long pivot = (range.a + range.b) / 2;

                seekable.seek(pivot);
                String line = seekable.readLine();
                if (line == null) // EOF
                    return null;

                // We may or may not hit a start of line.
                if (!isValidSHA1AndGAV(line))
                {
                    line = seekable.readLine();
                    if (line == null) // EOF
                        return null;
                    if (!isValidSHA1AndGAV(line))
                        throw new IllegalArgumentException("Invalid SHA1 GAV entry found:\n\t" + line);
                }

                // Compare the two SHA1 hashes (by bytes).
                System.arraycopy(line.substring(0, SHA1_LENGTH).getBytes(), 0, entryHashBytes, 0, SHA1_LENGTH);
                int compare = compare(hashBytes, entryHashBytes);
                if (compare == 0)
                    // Match
                    return line;
                if (compare < 0)
                    range.b = pivot;
                else
                    range.a = pivot;

                if (log.isLoggable(Level.FINEST))
                    log.finest("Range: " + range);

            }
            while (true);
        }

    }

    public static int compare(byte[] left, byte[] right)
    {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++)
        {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b)
                return a - b;
        }
        return left.length - right.length;
    }

    private static String lookForEntry_LinearSearch(String sha1hash, RandomAccessFile seekable, Range range) throws IOException
    {
        seekable.seek(range.a);
        while (true)
        {
            // Reached the end of range?
            if (seekable.getFilePointer() >= range.b)
                return null;

            String line = seekable.readLine();
            if (line == null) // EOF
                return null;

            // We may or may not hit the start of the line on the first turn.
            if (!isValidSHA1AndGAV(line))
                continue;

            // Compare the two SHA1 hashes (by bytes).
            if (0 == sha1hash.compareToIgnoreCase(line.substring(0, SHA1_LENGTH)))
                return line;
        }
    }

    private static final String ID = "([\\w-]+(\\.[\\w-]+)*)";

    // groupId:artifactId[:packaging[:classifier]]:version
    private static final Pattern PATTERN_SHA1_GAV = Pattern.compile(
                "\\p{XDigit}{" + SHA1_LENGTH + "} $ID:$ID:$ID(:$ID?(:$ID)?)?".replace("$ID", ID));

    /**
     * Validates the line against the expected format - "SHA1 groupId:artifactId[:packaging[:classifier]]:version".
     */
    public static boolean isValidSHA1AndGAV(String line)
    {
        return PATTERN_SHA1_GAV.matcher(line).matches();
    }

    /**
     * Just a data holder for a simple numeric range.
     */
    private static class Range
    {
        long a;
        long b;

        public Range(long a, long b)
        {
            this.a = a;
            this.b = b;
        }

        public long diff()
        {
            return this.b - this.a;
        }

        public Range set(long a, long b)
        {
            this.a = a;
            this.b = b;
            return this;
        }

        @Override
        public String toString()
        {
            return "Range " + a + " -- " + b;
        }
    }
}
