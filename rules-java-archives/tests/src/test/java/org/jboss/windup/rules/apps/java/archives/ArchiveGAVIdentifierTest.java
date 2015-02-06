package org.jboss.windup.rules.apps.java.archives;

import java.io.File;
import java.io.IOException;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.rules.apps.java.archives.identify.IdentifiedArchives;
import org.jboss.windup.rules.apps.java.archives.ignore.SkippedArchives;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveGAVIdentifierTest
{
    private static final String DATA_PATH = "src/test/resources/";
    private static final File SKIPLIST_FILE = new File(DATA_PATH + "/skippedArchives.txt");

    @Test
    public void testIdentifyArchive() throws IOException
    {
        final File mappingFile = new File(DATA_PATH + "/test.archive-metadata.txt");
        IdentifiedArchives.addMappingsFrom(mappingFile);
        Coordinate gav = IdentifiedArchives.getCoordinateFromSHA1("11856de4eeea74ce134ef3f910ff8d6f989dab2e");
        Assert.assertNotNull("IdentifiedArchives.getGAVFromSHA1 returned something", gav);
        Assert.assertEquals("org.jboss.windup", gav.getGroupId());
        Assert.assertEquals("windup-bootstrap", gav.getArtifactId());
        Assert.assertEquals("2.0.0.Beta7", gav.getVersion());
        Assert.assertEquals(null, gav.getClassifier());
    }

    @BeforeClass
    public static void loadSkipList()
    {
        SkippedArchives.load(SKIPLIST_FILE);
    }

    @Test
    public void testSkippedArchivesLoaded() throws IOException
    {
        Assert.assertNotEquals(0, SkippedArchives.getCount());
    }

    @Test
    public void testArtifactIdSuffixWithWildcards() throws IOException
    {
        // org.jboss.windup.*:*:*
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.jboss.windup:windup-foo:1.2.3")));
        // org.apache.commons.*:*:*
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.apache.commons.foo:commons-foo:1.2.3")));
    }

    @Test
    public void testGroupIdSuffixWithVersionAndClassifierWildcard() throws IOException
    {
        // org.jboss.bar:bar-*:*:*
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.jboss.bar:bar-foo:1.2.3")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.jboss.bar:just-foo:1.2.3")));

    }

    @Test
    public void testVersionRangeAndArtifactIdSuffixPattern() throws IOException
    {
        // org.hibernate.*:hibernate-core:[3.0,5.0)
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.hibernate.foo:hibernate-core:3.2.1")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.hibernate.foo:hibernate-core:1.2.3")));
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.hibernate.foo:hibernate-core:4.2.1")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.hibernate.foo:hibernate-core:5.0")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.hibernate.foo:hibernate-core:5.2.5-SNAPSHOT")));
    }

    @Test
    public void testAllButClassifier() throws IOException
    {
        // org.freemarker:freemarker-core:3.1
        Assert.assertTrue(SkippedArchives.isSkipped(CoordinateBuilder.create("org.freemarker.foo:freemarker-core:3.1")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.freemarker.foo:freemarker-core:3.1.1")));
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("org.freemarker:freemarker-core:4.2.1")));
    }

    @Test
    public void testUnlistedArchivesAreNotSkipped() throws IOException
    {
        Assert.assertFalse(SkippedArchives.isSkipped(CoordinateBuilder.create("com.example.foo:example-foo:1.2.3")));
    }
}