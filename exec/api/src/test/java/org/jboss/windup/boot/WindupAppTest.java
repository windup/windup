package org.jboss.windup.boot;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Test;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupAppTest
{
    @Test(expected = Exception.class)
    public void testMain() throws IOException
    {
        FileUtils.forceMkdir(Paths.get("server1").toFile());
        FileUtils.forceMkdir(Paths.get("server2").toFile());
        FileUtils.forceMkdir(Paths.get("WindupReport").toFile());
        
        String[] args = {
            "--srcServer.dir=server1",
            "--destServer.dir=server2",
            "--report.dir=WindupReport",
        };
        WindupApp.main(args);
    }
}
