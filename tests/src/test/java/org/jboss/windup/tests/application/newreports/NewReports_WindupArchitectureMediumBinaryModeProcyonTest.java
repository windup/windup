package org.jboss.windup.tests.application.newreports;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureMediumBinaryModeProcyonTest extends NewReports_WindupArchitectureMediumBinaryModeTest {
    @Test
    @Ignore
    public void testRunWindupMediumWithProcyon() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        try (GraphContext context = createGraphContext()) {
            Properties props = System.getProperties();
            props.setProperty("windup.decompiler", "Procyon");
            super.runTest(context, true, path, false);
            props.remove("windup.decompiler");
            allDecompiledFilesAreLinked(context);
        }
    }
}
