package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class EnableTattletaleParameterTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public final TemporaryFolder tmpAddonDir = new TemporaryFolder();

    @Test
    public void enableTattletaleParameter() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(), 
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--enableTattletale");

        assertTrue(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
        assertTrue(capturedOutput().contains("WARNING: (DEPRECATED) --enableTattletale option is not necessary anymore since Tattletale report generation is enabled by default. Use only --disableTattletale option if you want to disable it."));
    }
}
