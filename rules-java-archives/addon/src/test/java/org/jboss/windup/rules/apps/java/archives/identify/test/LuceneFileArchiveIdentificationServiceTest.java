package org.jboss.windup.rules.apps.java.archives.identify.test;

import java.io.File;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.rules.apps.java.archives.identify.ArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.LuceneArchiveIdentificationService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class LuceneFileArchiveIdentificationServiceTest {

    @Test
    public void testGetCoordinateFromSHA1() throws Exception {
        final File file = new File("target/test-nexus-data/lucene/");
        Assert.assertTrue("Test file does not exist", file.exists());
        LuceneArchiveIdentificationService ident = new LuceneArchiveIdentificationService(file);

        Coordinate coordinate = ident.getCoordinate("55555555564e84315e83c6ba4a855b07ba51166b");
        Assert.assertNull("No coordinate for 55555555564e84315e83c6ba4a855b07ba51166b", coordinate);

        // Position 0
        check(ident, "8aab519d6654d378f94c612e19495bbcb9d355e5", "com.google.apis:google-api-services-genomics:jar::v1-rev623-1.25.0");

        // Last entry
        check(ident, "ffffdf1558b62750b24bdaa33cb9a72b0cb766ce", "org.glassfish.metro:wsmc-impl:jar::2.1.1-b06");

        // A block around pivot break.
        check(ident, "4e02fd52064e84315e83c6ba4a855b07ba51166b", "org.jogamp.joal:joal:jar:natives-macosx-universal:2.1.2");
        check(ident, "4e031603849ad1e70d245855802cc388ded93461", "org.glassfish.jdbc.jdbc-ra.jdbc40:jdbc40:jar::3.0-b37");
        // Position 29723213
        check(ident, "4e031bb61df09069aeb2bffb4019e7a5034a4ee0", "junit:junit:jar::4.11");
        check(ident, "4e0334465984c00cbcf177b1702805bd4b5d6d27", "org.soitoolkit.refapps.sd:soitoolkit-refapps-sample-schemas:jar::0.6.1");
        check(ident, "4e034d862d9650df285b8ee98f7f770db6c19029", "org.apache.cxf:cxf-rt-bindings-soap:jar::2.4.8");

        // Some which caused issues.
        check(ident, "7ff0d167a6816aa113b1b4a8a37515701a74b288", "org.kill-bill.billing:killbill-platform-osgi-bundles-lib-slf4j-osgi:jar::0.1.0");

        // https://issues.redhat.com/browse/WINDUP-2765
        check(ident, "85f79121fdaabcbcac085d0d4aad34af9f8dbba2", "org.springframework.boot:spring-boot-starter-web:jar::2.3.2.RELEASE");
        // https://issues.redhat.com/browse/WINDUP-2765
        check(ident, "dff5c6bcfd0606124cbb1e6050563dc96a967bce", "org.apache.ant:ant-commons-logging:jar::1.8.0");
        // https://issues.redhat.com/browse/WINDUP-3300
        check(ident, "d6153f8fc60c479ab0f9efb35c034526436a4953", "com.fasterxml.jackson.core:jackson-databind:jar::2.12.3");
    }

    private static void check(ArchiveIdentificationService ident, String hash, String coordString) {
        Coordinate coord = ident.getCoordinate(hash);
        Assert.assertNotNull("Coordinate not found for " + hash, coord);
        Assert.assertEquals(hash + " = " + coordString, coordString, coordToString(coord));
    }


    // GROUP_ID:ARTIFACT_ID[:PACKAGING[:CLASSIFIER]]:VERSION
    private static String coordToString(Coordinate coord) {
        StringBuilder sb = new StringBuilder();
        sb.append(coord.getGroupId()).append(':').append(coord.getArtifactId());
        if (coord.getPackaging() != null)
            sb.append(':').append(coord.getPackaging());
        if (coord.getClassifier() != null)
            sb.append(':').append(coord.getClassifier());
        sb.append(':').append(coord.getVersion());
        return sb.toString();
    }

}
