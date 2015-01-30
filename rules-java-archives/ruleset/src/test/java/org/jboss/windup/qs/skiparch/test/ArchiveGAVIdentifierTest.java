package org.jboss.windup.qs.skiparch.test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.jboss.windup.qs.identarch.lib.ArchiveGAVIdentifier;
import org.jboss.windup.qs.identarch.model.GAV;
import org.jboss.windup.qs.skiparch.lib.SkippedArchives;
import org.jboss.windup.util.Logging;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the library itself.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ArchiveGAVIdentifierTest
{
    private static final Logger log = Logging.get(ArchiveGAVIdentifierTest.class);

    private static final String PKG_PATH  = ArchiveGAVIdentifierTest.class.getPackage().getName().replace('.', '/');
    private static final String DATA_PATH = "src/test/java/" + PKG_PATH + "/data";

    
    @Test
    public void testIdentifyArchive() throws IOException
    {

        final File mappingFile = new File(DATA_PATH + "/sha1ToGAV.txt");
        if (!mappingFile.exists())
            throw new IllegalStateException("Test file does not exist: " + mappingFile);
        ArchiveGAVIdentifier.addMappingsFrom(mappingFile);
        GAV gav = ArchiveGAVIdentifier.getGAVFromSHA1("11856de4eeea74ce134ef3f910ff8d6f989dab2e");
        Assert.assertNotNull("ArchiveGAVIdentifier.getGAVFromSHA1 returned something", gav);
        Assert.assertEquals("org.jboss.windup", gav.getGroupId());
        Assert.assertEquals("windup-bootstrap", gav.getArtifactId());
        Assert.assertEquals("2.0.0.Beta7", gav.getVersion());
        Assert.assertEquals(null, gav.getClassifier());
    }

    @Test
    public void testSkippedArchives() throws IOException
    {
        final File skipListFile = new File(DATA_PATH + "/skippedArchives.txt");
        if (!skipListFile.exists())
            throw new IllegalStateException("Test file does not exist: " + skipListFile);

        SkippedArchives.addSkippedArchivesFrom(skipListFile);
        log.info("Skipped archives count: " + SkippedArchives.getCount());
        Assert.assertNotEquals("There are some skipped archives", 0, SkippedArchives.getCount());

        // org.jboss.windup.*:*:*
        GAV gav = new GAV("org.jboss.windup","windup-foo","1.2.3");
        Assert.assertTrue("GAV is skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // org.apache.commons.*:*:*
        gav = new GAV("org.apache.commons.foo","commons-foo","1.2.3");
        Assert.assertTrue("GAV is skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // org.jboss.bar:bar-*:*:*
        gav = new GAV("org.jboss.bar","bar-foo","1.2.3");
        Assert.assertTrue("GAV is skipped: " + gav,  SkippedArchives.isSkipped(gav));
        gav = new GAV("org.jboss.bar","just-foo","1.2.3");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // org.hibernate.*:hibernate-core:3.*~4.*
        gav = new GAV("org.hibernate.foo","hibernate-core","3.2.1");
        Assert.assertTrue("GAV is skipped: " + gav,  SkippedArchives.isSkipped(gav));
        gav = new GAV("org.hibernate.foo","hibernate-core","4.2.1");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // org.hibernate.*:hibernate-core:3.*~4.*
        gav = new GAV("org.hibernate.foo","hibernate-core","1.2.3");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // org.freemarker:freemarker-core:3.1
        gav = new GAV("org.freemarker.foo","freemarker-core","3.1");
        Assert.assertTrue("GAV is skipped: " + gav,  SkippedArchives.isSkipped(gav));
        gav = new GAV("org.freemarker.foo","freemarker-core","3.1.1");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));
        gav = new GAV("org.freemarker","freemarker-core","4.2.1");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));

        // Not listed at all
        gav = new GAV("cz.dynawest.foo","dynawest-foo","1.2.3");
        Assert.assertFalse("GAV is not skipped: " + gav,  SkippedArchives.isSkipped(gav));
    }



}// class
