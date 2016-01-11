package org.jboss.windup.tests.bootstrap;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DisplayHelpCommandTest extends AbstractBootstrapTest {
    @Test
    public void noArgument() {
        test();
    }

    @Test
    public void longArgument() {
        test("--help");
    }

    @Test
    public void shortArgument() {
        test("-h");
    }

    private void test(String... args) {
        bootstrap(args);

        assertTrue(capturedOutput().contains("Windup Options:"));
        assertTrue(capturedOutput().contains("--updateRulesets"));
        assertTrue(capturedOutput().contains("Forge Options:"));
        assertTrue(capturedOutput().contains("--version"));
    }
}
