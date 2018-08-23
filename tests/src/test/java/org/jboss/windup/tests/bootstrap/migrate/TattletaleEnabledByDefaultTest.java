package org.jboss.windup.tests.bootstrap.migrate;

import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.rules.apps.tattletale.TattletaleRuleProvider;
import org.jboss.windup.tests.bootstrap.AbstractBootstrapTestWithRules;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.enterprise.inject.Vetoed;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TattletaleEnabledByDefaultTest extends AbstractBootstrapTestWithRules {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public final TemporaryFolder tmpAddonDir = new TemporaryFolder();

    @Test
    public void tattletaleEnabledByDefault() {
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
    public void bothTattletaleParameters() {
        bootstrap("--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--install", "org.jboss.windup.rules.apps:windup-rules-tattletale," + Bootstrap.getVersion());

        bootstrap("--input", "../test-files/Windup1x-javaee-example-tiny.war",
                "--output", tmp.getRoot().getAbsolutePath(),
                "--source", "eap6",
                "--target", "eap7",
                "--addonDir", tmpAddonDir.getRoot().getAbsolutePath(),
                "--enableTattletale",
                "--disableTattletale");

        assertFalse(Files.exists(Paths.get(tmp.getRoot().getAbsolutePath(), "reports", "tattletale")));
        assertTrue(capturedOutput().contains("WARNING: (DEPRECATED) --enableTattletale option is not necessary anymore since Tattletale report generation is enabled by default. Use only --disableTattletale option if you want to disable it."));
        assertTrue(capturedOutput().contains("ERROR: Do NOT use --enableTattletale and --disableTattletale options together. Tattletale report generation is enabled by default so --enableTattletale option should be removed."));
        assertFalse(capturedOutput().contains("Executing RHAMT"));
    }

}
