package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.util.Logging;

/**
 * Identifies an archive by it's Maven G:A:V coordinates from some source;
 * Currently, the data is loaded from a static file, which is generated from JBoss.org repository.
 * The values are also stored as a String to spare some memory - we won't need most of the data.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ArchiveGAVIdentifier
{
    private static final Logger log = Logging.get(ArchiveGAVIdentifier.class);

    private static final Map<String, String> gavs = new HashMap();

    private static final String DEFAULT_MAPPINGS_FILENAME = "jboss.sha1ToGAV.txt";

    static {
        try
        {
            if(new File(DEFAULT_MAPPINGS_FILENAME).exists())
                addMappingsFrom(new FileInputStream(DEFAULT_MAPPINGS_FILENAME));
        }
        catch( FileNotFoundException ex )
        {
            Logging.get(ArchiveGAVIdentifier.class).log(Level.SEVERE, null, ex);
        }
    }


    public static void addMappingsFromZip(InputStream is)
    {
        addMappingsFrom(new ZipInputStream(is));
    }

    public ArchiveGAVIdentifier()
    {
    }


    public static GAV getGAVFromSHA1(String sha1Hash)
    {
        String gavStr = gavs.get(sha1Hash);
        if(null == gavStr)
            return null;

        return GAV.fromGAV(gavStr);
    }

    public static void addMapping(String sha1, String gavc)
    {
        gavs.put(sha1, gavc);
    }


    public static void addMappingsFrom(File file)
    {
        try
        {
            addMappingsFrom(new FileInputStream(file));
        }
        catch (FileNotFoundException ex)
        {
            log.log(Level.SEVERE, "Failed loading SHA1 to GAV mapping from " + file.toString() + ": " + ex.getMessage(), ex);
        }
    }

    public static void addMappingsFrom(InputStream is)
    {
        try
        {
            LineIterator it = IOUtils.lineIterator(is, "UTF-8");
            while (it.hasNext())
            {
                String line = it.next();
                if (line.startsWith("#"))
                    continue; // Skip comments
                String[] parts = StringUtils.split(line, ' ');
                if (parts.length < 2)
                    throw new IllegalArgumentException("Expected GAV definition format is 'SHA1 G:A:V[:C]', was: " + line);
                //GAV gav = GAV.from(line);
                addMapping(parts[0], parts[1]);
            }
        }
        catch (IOException ex)
        {
            log.log(Level.SEVERE, "Failed loading SHA1 to GAV mapping: " + ex.getMessage(), ex);
        }
    }

}// class
