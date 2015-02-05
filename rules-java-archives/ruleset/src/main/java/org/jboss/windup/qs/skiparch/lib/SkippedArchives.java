package org.jboss.windup.qs.skiparch.lib;

import org.jboss.windup.qs.identarch.lib.ArchiveGAVIdentifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.jboss.forge.furnace.versions.DefaultVersionRange;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.VersionRange;
import org.jboss.windup.qs.identarch.model.GAVModel;
import org.jboss.windup.util.Logging;

/**
 * A service class keeping the set of skipped archives and handling their lookup.
 *
 * Data file format:
 *   G:A:V[~V][:C]
 *
 * Examples:
 *   org.apache.commons.*:*:*
 *   org.hibernate.*:hibernate-core:3.*~4.*
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class SkippedArchives
{
    private static final Logger log = Logging.get(ArchiveGAVIdentifier.class);

    private static final String DEFAULT_SKIPPED_ARCHIVES_FILENAME = "skippedArchives.txt";

    private static final Map<String, GAVWithVersionRange> skippedArtifactsDefinitions = new HashMap();

    static {
        try
        {
            if(new File(DEFAULT_SKIPPED_ARCHIVES_FILENAME).exists())
                addSkippedArchivesFrom(new FileInputStream(DEFAULT_SKIPPED_ARCHIVES_FILENAME));
        }
        catch( FileNotFoundException ex )
        {
            throw new IllegalStateException("Can't load data from " + DEFAULT_SKIPPED_ARCHIVES_FILENAME + ": " + ex.getMessage());
        }
    }


    public static boolean isSkipped(GAVModel archiveGav)
    {
        for(GAVWithVersionRange def : skippedArtifactsDefinitions.values())
        {
            if(!isPatternMatch(def.getGroupId(), archiveGav.getGroupId()))
                continue;

            if(!isPatternMatch(def.getArtifactId(), archiveGav.getArtifactId()))
                continue;

            // Single version
            if(def.getVersionMax() == null)
            {
                if(!isPatternMatch(def.getVersion(), archiveGav.getVersion()))
                    continue;
            }
            // Version range
            else
            {
                VersionRange range = new DefaultVersionRange(
                        new SingleVersion(def.getVersion()), true,
                        new SingleVersion(def.getVersionMax()), false
                );
                if(!range.includes(new SingleVersion(archiveGav.getVersion())))
                    continue;
            }

            return true;
        }

        return false;
    }


    public static void addSkippedArchivesFrom(File skippedArchFile)
    {
        try
        {
            addSkippedArchivesFrom(new FileInputStream(skippedArchFile));
        }
        catch( FileNotFoundException ex )
        {
            log.log(Level.SEVERE, "Failed loading SHA1 to GAV mapping from " + skippedArchFile.toString() + ": " + ex.getMessage(), ex);
        }
    }

    public static void addSkippedArchivesFrom(InputStream is)
    {
        try
        {
            LineIterator it = IOUtils.lineIterator(is, "UTF-8");
            while( it.hasNext() )
            {
                String line = it.next();
                if(line.startsWith("#"))
                    continue; // Skip comments
                //String[] parts = StringUtils.split(line, ':');
                //if(parts.length < 3)
                //    throw new IllegalArgumentException("Expected GAV definition format is 'G:A:V[~V][:C]', was: " + line);
                final GAVWithVersionRange gavv = GAVWithVersionRange.fromGAVV(line);
                skippedArtifactsDefinitions.put(gavv.getGroupId(), gavv);
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(ArchiveGAVIdentifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static int getCount()
    {
        return skippedArtifactsDefinitions.size();
    }



    private static boolean isPatternMatch(String pattern, String examinedId)
    {
        // "*"
        if("*".equals(pattern))
            return true;

        // Exact match
        if(pattern.equals(examinedId))
            return true;

        // Prefix - org.foo.*
        if(pattern.endsWith("*"))
            if(examinedId.startsWith(pattern.substring(0, pattern.length()-1)))
                return true;

        return false;
    }


}// class
