package org.jboss.windup.tests.application.newreports;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureMediumBinaryModeFernflowerTest extends WindupArchitectureMediumBinaryModeTest {
    @Test
    public void testRunWindupMediumWithFernflower() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, false, path, false);
            allDecompiledFilesAreLinked(context);
            validateManifestEntries(context);
            validateReports(context);
        }

    }
}
