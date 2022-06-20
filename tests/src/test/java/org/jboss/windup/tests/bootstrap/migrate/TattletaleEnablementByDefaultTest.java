package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TattletaleEnablementByDefaultTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public final TemporaryFolder tmpAddonDir = new TemporaryFolder();

    @Test
    public void tattletaleEnabledByDefaultWithEapTarget() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath());

        assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
    }

    @Test
    public void tattletaleDisabledByDefaultWithoutEapTarget() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "cloud-readiness",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath());

        assertFalse(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
    }

    @Test
    public void bothTattletaleParametersWithEapTarget() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--enableTattletale",
                "--disableTattletale");

        assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
        assertTrue(capturedOutput().contains("INFO: --disableTattletale option ignored since --enableTattletale option has been provided as well."));
        assertFalse(capturedOutput().contains("INFO: --enableTattletale option can be removed since Tattletale report generation is enabled by default when JBoss EAP is one of the analysis targets."));
    }

    @Test
    public void bothTattletaleParametersWithoutEapTarget() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--target", "cloud-readiness",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--enableTattletale",
                "--disableTattletale");

        assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
        assertTrue(capturedOutput().contains("INFO: --disableTattletale option ignored since --enableTattletale option has been provided as well."));
        assertFalse(capturedOutput().contains("INFO: --enableTattletale option can be removed since Tattletale report generation is enabled by default when JBoss EAP is one of the analysis targets."));
    }

}
