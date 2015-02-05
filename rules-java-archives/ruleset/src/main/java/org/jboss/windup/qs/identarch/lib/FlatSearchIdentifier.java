package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Upon construction, reads the whole file and creates a SHA1 -> GAV map.
 * Then just returns from it.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class FlatSearchIdentifier implements HashToGAVIdentifier
{
    private final InputStream is;

    // SHA1 hash -> file position where it is.
    // Not sorted - we expect more entries in the file than in the scanned apps.
    private Map<String, String> sha1ToGavMap = new HashMap();


    public FlatSearchIdentifier(InputStream is)
    {
        this.is = is;
        try
        {
            readEntries();
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Could not find SHA1 to GAV entries: " + ex.getMessage(), ex);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public GAV getGAVFromSHA1(String sha1Hash)
    {
        String entry = this.sha1ToGavMap.get(sha1Hash);
        if (null == entry)
            return null;
        return GAV.fromSHA1AndGAV(entry);
    }


    /**
     * Reads all entries from this' InputStream and puts them into the map.
     */
    private void readEntries() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.is));
        while (true) {
            String line = reader.readLine();
            //GAV.fromSHA1AndGAV(line); // Rather Parsing on demand.
            String hash = line.substring(0, SHA1_LENGTH);
            this.sha1ToGavMap.put(hash, line.substring(0, MAX_ENTRY_LENGTH));
        }
    }



}// class
