package org.jboss.windup.rules.victims.test;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.util.Logging;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the library itself.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class VictimsLibTest
{
    private static final Logger log = Logging.get(VictimsLibTest.class);


    // Path to a jar known to contain a vulnerability.
    private static final String BAD_JAR = "target/testJars/xercesImpl-2.9.1.jar";

    // SHA-512 checksum of xerces:xercesImpl:2.9.1 - not present in Victims db.
    private static final String BAD_JAR_SHA512 = "ec2200e5a5a70f5c64744f6413a546f5e4979b3fb1649b02756ff035d36dde31170eaadc70842230296b60896f04877270c26b40415736299aef44ac16c5811c";

    // Contained in FILEHASHES table.
    private static final String BAD_SHA512 = "1a2ee5525a4525e1df8b7e43e3b95992122ef478990acedb77a345929bc1ca435d614e12202a24fc9f9a038ee9d0a39e5a46b831c49857ca198cf4eafd7b515b";


    @Test
    public void testUpdate() throws IOException, VictimsException
    {
        // By default, it would go to ~/.victims)
        final String victimsDBdir = "target/victimsDB";
        System.setProperty(VictimsConfig.Key.HOME, victimsDBdir);

        FileUtils.deleteQuietly(new File(victimsDBdir));
        try
        {
            VictimsDBInterface db = VictimsDB.db();
            int recordCount = db.getRecordCount();
            System.out.println("  DB records:   " + recordCount);
            System.out.println("  Database last updated on: " + db.lastUpdated().toString());
            Assert.assertEquals(0, recordCount);

            System.out.println("  Syncing...");
            db.synchronize();
            recordCount = db.getRecordCount();
            System.out.println("  DB records:   " + recordCount);
            Assert.assertTrue(recordCount > 200);
            System.out.println("  Database last updated on: " + db.lastUpdated().toString());
        }
        catch (VictimsException ex)
        {
            // Prevent failure if offline. Just a warning.
            if ("Failed to sync database".equals(ex.getMessage()))
                log.warning(ex.getMessage());
            else
                throw ex;
        }
    }

    @Test
    public void testMatch() throws IOException, VictimsException
    {
        VictimsDBInterface db = VictimsDB.db();
        HashSet<String> vulnerabilities = db.getVulnerabilities(BAD_SHA512);
        Assert.assertTrue(vulnerabilities.contains("CVE-2011-2730"));
    }
}
