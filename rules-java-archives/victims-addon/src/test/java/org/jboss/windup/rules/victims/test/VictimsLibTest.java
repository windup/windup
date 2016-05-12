package org.jboss.windup.rules.victims.test;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import java.io.IOException;
import java.util.logging.Logger;
import org.jboss.windup.util.Logging;
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

    // SHA-512 checksum of xerces:xercesImpl:2.9.1
    private static final String BAD_JAR_SHA512 = "ec2200e5a5a70f5c64744f6413a546f5e4979b3fb1649b02756ff035d36dde31170eaadc70842230296b60896f04877270c26b40415736299aef44ac16c5811c";

    // Contained in FILEHASHES table.
    private static final String BAD_SHA512 = "851eba12748a1aada5829e3a8e2eba05435efaaef9f0e7f68f6246dc1f6407ca56830ef00d587e91c3d889bb70eaf605a305652479ba6986a90b3986f0e74daf";


    @Test
    public void testUpdate() throws IOException, VictimsException
    {
        try {
            VictimsDBInterface db = VictimsDB.db();
            System.out.println(" DB records:   " + db.getRecordCount());
            System.out.println(" Syncing...");
            // Update (goes to ~/.victims)
            db.synchronize();
            System.out.println(" DB records:   " + db.getRecordCount());
            System.out.println("Database last updated on: " + db.lastUpdated().toString());
        }
        catch (VictimsException ex){
            // Prevent failure if offline. Just a warning.
            if ("Failed to sync database".equals(ex.getMessage()))
                log.warning(ex.getMessage());
            else
                throw ex;
        }
    }

}
