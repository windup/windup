package org.jboss.windup.tests.bootstrap;

import org.jboss.windup.bootstrap.Bootstrap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class InstallRemoveAddonCommandsTest extends AbstractBootstrapTest {
    private static final String ADDON = "org.jboss.windup.rules.apps:windup-rules-tattletale";
    private static final String ADDON_WITH_VERSION = ADDON + "," + Bootstrap.getVersion();
    private static final String BAD_FORMAT = "doesnt.exist";
    private static final String DOESNT_EXIST = "doesnt:exist";

    private static final String INSTALL = "--install";
    private static final String REMOVE = "--remove";
    private static final String I = "-i";
    private static final String R = "-r";

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {I,       ADDON,              R,      ADDON,              true},
                {I,       ADDON,              R,      ADDON_WITH_VERSION, true},
                {I,       ADDON,              REMOVE, ADDON,              true},
                {I,       ADDON,              REMOVE, ADDON_WITH_VERSION, true},
                {I,       ADDON_WITH_VERSION, R,      ADDON,              true},
                {I,       ADDON_WITH_VERSION, R,      ADDON_WITH_VERSION, true},
                {I,       ADDON_WITH_VERSION, REMOVE, ADDON,              true},
                {I,       ADDON_WITH_VERSION, REMOVE, ADDON_WITH_VERSION, true},
                {INSTALL, ADDON,              R,      ADDON,              true},
                {INSTALL, ADDON,              R,      ADDON_WITH_VERSION, true},
                {INSTALL, ADDON,              REMOVE, ADDON,              true},
                {INSTALL, ADDON,              REMOVE, ADDON_WITH_VERSION, true},
                {INSTALL, ADDON_WITH_VERSION, R,      ADDON,              true},
                {INSTALL, ADDON_WITH_VERSION, R,      ADDON_WITH_VERSION, true},
                {INSTALL, ADDON_WITH_VERSION, REMOVE, ADDON,              true},
                {INSTALL, ADDON_WITH_VERSION, REMOVE, ADDON_WITH_VERSION, true},

                {INSTALL, BAD_FORMAT,         REMOVE, BAD_FORMAT,         false},
                {INSTALL, DOESNT_EXIST,       REMOVE, DOESNT_EXIST,       false},
        });
    }

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private final String installOption;
    private final String installAddonId;
    private final String removeOption;
    private final String removeAddonId;
    private final boolean successExpected;

    public InstallRemoveAddonCommandsTest(String installOption, String installAddonId, String removeOption,
                                          String removeAddonId, boolean successExpected) {
        this.installOption = installOption;
        this.installAddonId = installAddonId;
        this.removeOption = removeOption;
        this.removeAddonId = removeAddonId;
        this.successExpected = successExpected;
    }

    @Test
    public void commaSeparatedVersion() throws IOException {
        bootstrap("--addonDir", tmp.getRoot().getAbsolutePath(), installOption, installAddonId, removeOption, removeAddonId);
        checkExpectations();
    }

    @Test
    public void colonSeparatedVersion() throws IOException {
        String installAddonId = this.installAddonId.replace(',', ':');
        String removeAddonId = this.removeAddonId.replace(',', ':');

        bootstrap("--addonDir", tmp.getRoot().getAbsolutePath(), installOption, installAddonId, removeOption, removeAddonId);
        checkExpectations();
    }

    private void checkExpectations() {
        if (successExpected) {
            assertTrue(capturedOutput().contains("Installation completed successfully"));
            assertTrue(capturedOutput().contains("Uninstallation completed successfully"));
        } else {
            if (BAD_FORMAT.equals(installAddonId)) {
                assertTrue(capturedOutput().contains("Unrecognized format"));
            } else {
                assertTrue(capturedOutput().contains("No Artifact version found"));
            }

            assertTrue(capturedOutput().contains("No addon exists"));
        }
    }
}
