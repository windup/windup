package org.jboss.windup.tests.bootstrap;

import org.jboss.windup.bootstrap.Bootstrap;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DisplayVersionCommandTest extends AbstractBootstrapTest {
    @Test
    public void longArgument() {
        test("--version");
    }

    @Test
    public void shortArgument() {
        test("-v");
    }

    private void test(String... args) {
        bootstrap(args);

        assertTrue(capturedOutput().contains(Bootstrap.getVersion()));
    }
}
