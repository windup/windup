package org.jboss.windup.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ZipUtilTest {

    @Test
    public void testScanZipFile() throws URISyntaxException {
        // TLS1.2_setting.zip has a file that contains 3 files, one of them is not readable so it will be skipped
        URI uri = Thread.currentThread().getContextClassLoader().getResource("TLS1.2_setting.zip").toURI();
        Path filePath = Paths.get(uri);

        List<String> files = ZipUtil.scanZipFile(filePath, true);

        Assert.assertEquals(2, files.size());
        Assert.assertTrue(files.contains("file1.txt"));
        Assert.assertTrue(files.contains("file2.txt"));
    }
}