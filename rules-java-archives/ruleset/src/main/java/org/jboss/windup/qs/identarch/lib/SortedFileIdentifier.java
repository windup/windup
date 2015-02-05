package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class SortedFileIdentifier implements HashToGAVIdentifier
{
    // Input
    private File dataFile;

    // Work variables
    private final long dataSize;
    private FileInputStream dataFileIS = null;

    // SHA1 hash -> file position where it is.
    private SortedMap<String, Long> posCache = new TreeMap();


    public SortedFileIdentifier(File dataFile) throws FileNotFoundException
    {
        if (!dataFile.exists())
            throw new IllegalArgumentException("Hash to GAV data file does not exist: " + dataFile.toString());
        this.dataFile = dataFile;
        this.dataSize = this.dataFile.length();
        this.dataFileIS = new FileInputStream(dataFile);
    }




    @Override
    public GAV getGAVFromSHA1(String sha1Hash)
    {
        try
        {
            Range range = findRawSeekRange(sha1Hash);
            String gavRawData = lookForEntry_PivotSearch(sha1Hash, range);
            if (null == gavRawData)
                return null;
            return GAV.fromSHA1AndGAV(gavRawData);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Could not find SHA1 to GAV entries: " + ex.getMessage(), ex);
        }
    }


    /**
     * Returns the range in the file where to look for given hash.
     * This is basically a binary search in a cache,
     * which is an ordered Map: SHA1 hash -> position in the file
     * (assuming the file is ordered!).
     */
    private Range findRawSeekRange(String sha1Hash)
    {
        if(this.posCache.size() == 0)
            return new Range(0, this.dataSize);

        sha1Hash = sha1Hash.toLowerCase();

        Set<String> keySet = this.posCache.keySet();
        List<String> keys = new ArrayList(keySet);

        Range cacheIndexRange = new Range(0, this.posCache.size()-1);
        Range fileSeekRange = new Range(this.posCache.get(this.posCache.firstKey()), this.posCache.get(this.posCache.lastKey()));

        while (cacheIndexRange.diff() > 1) {
            long pivot = (cacheIndexRange.a + cacheIndexRange.b) / 2;
            final String hashAtPivot = keys.get((int)pivot);
            int comp = sha1Hash.compareTo(hashAtPivot);
            if(comp == 0) // The exact hash. What a chance.
            {
                final Long offsetAtPivot = this.posCache.get(hashAtPivot);
                return fileSeekRange.set(offsetAtPivot, offsetAtPivot);
            }
            if(comp < 0)
                cacheIndexRange.b = pivot;
            else
                cacheIndexRange.a = pivot;
        }

        return new Range(this.posCache.get(keys.get((int) cacheIndexRange.a)), this.posCache.get(keys.get((int)cacheIndexRange.b)));
    }


    /**
     * @return A single entry for given hash in this SortedIdentifier's file within the given seek range, or null if not found.
     */
    private String lookForEntry_PivotSearch(String sha1Hash, Range range) throws FileNotFoundException, IOException
    {
        final byte[] hashBytes = sha1Hash.getBytes();

        //FileReader reader = new FileReader(dataFile);
        RandomAccessFile seekable = new RandomAccessFile(this.dataFile, "r");

        final byte[] entryHashBytes = new byte[SHA1_LENGTH];

        do {
            if (range.diff() < 300) // The longest entry length estimation * 2.
                return lookForEntry_LinearSearch(sha1Hash, seekable, range);

            long pivot = (range.a + range.b) / 2;


            seekable.seek(pivot);
            //findNewLine(seekable);
            String line = seekable.readLine();
            if (line == null) // EOF
                return null;

            // We may or may not hit a start of line.
            if (!GAV.isValidSHA1AndGAV(line))
            {
                line = seekable.readLine();
                if (line == null) // EOF
                    return null;
                if (!GAV.isValidSHA1AndGAV(line))
                    throw new IllegalArgumentException("Invalid SHA1 GAV entry found:\n\t" + line);
            }

            // Compare the two SHA1 hashes (by bytes).
            System.arraycopy(line.substring(0, SHA1_LENGTH).getBytes(), 0, entryHashBytes, 0, SHA1_LENGTH);
            int compare = compare(hashBytes, entryHashBytes);
            if (compare == 0)
                // Match
                return line;
            if (compare < 0) {
                range.b = pivot;
            }
            else {
                range.a = pivot;
            }

        } while (true);
    }


    //UnsignedBytes.lexicographicalComparator()  needs Guava
    public static int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }


    /*
    private boolean findNewLine(RandomAccessFile seekable) throws IOException
    {
        do
        {
            int read = seekable.read();
            if (read == '\n' || read == '\r')
                return true;
            if (read < 0)
                return false;
        }
    }*/


    private String readRestOfLine(String sha1Hash, RandomAccessFile seekable) throws IOException
    {
        /*StringBuilder sb = new StringBuilder(sha1Hash);

        while(true) {
            int read = seekable.read();
            if (read == '\n' || read == '\r')
                break;
            if (read < 0)
                return sb.toString();
        }

        while(true) {
            int read = seekable.read();
            if (read != '\n' && read != '\r')
                seekable.
        }

        return sb.toString();
        */
        return sha1Hash + seekable.readLine();
    }


    private String lookForEntry_LinearSearch(String sha1hash, RandomAccessFile seekable, Range range) throws IOException
    {
        seekable.seek(range.a);
        while (true) {
            // Reached the end of range?
            if (seekable.getFilePointer() >= range.b)
                return null;

            String line = seekable.readLine();
            if (line == null) // EOF
                return null;

            // We may or may not hit a start of line on first turn.
            if (!GAV.isValidSHA1AndGAV(line))
                continue;


            // Compare the two SHA1 hashes (by bytes).
            /*System.arraycopy(line.substring(0,SHA1_LENGTH).getBytes(), 0, entryHashBytes, 0, SHA1_LENGTH);
            int compare = compare(hashBytes, entryHashBytes);
            if (compare == 0)
                // Match
                return line;
            */
            if (0 == sha1hash.compareToIgnoreCase(line.substring(0, SHA1_LENGTH)))
                return line;
        }

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

        public long diff(){
            return this.b - this.a;
        }

        public Range set(long a, long b)
        {
            this.a = a;
            this.b = b;
            return this;
        }
    }
}
