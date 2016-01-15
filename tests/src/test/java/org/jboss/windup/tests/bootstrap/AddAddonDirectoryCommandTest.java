package org.jboss.windup.tests.bootstrap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AddAddonDirectoryCommandTest extends AbstractBootstrapTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void mutable_longArgument() throws IOException {
        test("--addonDir");
    }

    @Test
    public void mutable_shortArgument() throws IOException {
        test("-a");
    }

    @Test
    public void immutable_longArgument() throws IOException {
        test("--immutableAddonDir");
    }

    @Test
    public void immutable_shortArgument() throws IOException {
        test("-m");
    }

    private void test(String arg) throws IOException {
        String tmpPath = tmp.getRoot().getAbsolutePath();

        bootstrap(arg, tmpPath, "--list");

        assertTrue(capturedOutput().contains(tmpPath));
    }
}
