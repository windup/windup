package org.jboss.windup.qs.skiparchives.nexusreader;

import java.io.File;
import java.io.FileWriter;
import java.util.logging.Logger;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * This is meant not only as a test, but also as the tool which prepares the mapping bundle (in next module).
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexToGavMappingConverterTest
{
    private static final Logger log = Logging.get(IndexToGavMappingConverterTest.class);

    private final boolean storeIndexInWindupHome;


    public IndexToGavMappingConverterTest()
    {
        this.storeIndexInWindupHome = "true".equals(System.getProperty("IdentArch.persistIndexData"));
    }

    private File getBaseDataDir(){
        if (!this.storeIndexInWindupHome)
            return new File("target/");
        else
            return WindupPathUtil.getWindupUserDir().resolve("temp/IdentArch/mavenReposIndexes/").toFile();
    }


    @Test
    public void testAUpdateIndex() throws Exception
    {
        final File dataDir = this.getBaseDataDir();
        final IndexToGavMappingConverter coverter = new IndexToGavMappingConverter(dataDir, "central", "http://repo1.maven.org/maven2");

        // Update the index (incremental update will happen if this is not 1st run and files are not deleted)
        log.info("Downloading or updating index into " + dataDir.getPath());
        coverter.updateIndex();
        coverter.close();
    }


    @Test
    public void testZPrintAllArtifacts() throws Exception
    {
        final File dataDir = this.getBaseDataDir();
        final File destDir = new File("target/");

        final IndexToGavMappingConverter coverter = new IndexToGavMappingConverter(dataDir, "central", "http://repo1.maven.org/maven2");

        final File shaToGavFile = new File(destDir, "central.SHA1toGAVs.txt");
        log.info("Printing all artifacts to " + shaToGavFile.getPath());
        coverter.printAllArtifacts(new FileWriter(shaToGavFile));
        coverter.close();
        log.info("Sorting " + shaToGavFile.getPath());
        IndexToGavMappingConverter.sortFile(shaToGavFile, new File(destDir, "central.SHA1toGAVs.sorted.txt"));
    }
}
